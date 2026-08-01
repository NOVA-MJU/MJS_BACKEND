#!/usr/bin/env python3
"""Evaluate notice semantic predictions without third-party dependencies."""

from __future__ import annotations

import argparse
import json
import math
from collections import defaultdict
from datetime import datetime
from pathlib import Path
from typing import Any, Iterable


def read_jsonl(path: Path) -> list[dict[str, Any]]:
    records: list[dict[str, Any]] = []
    with path.open(encoding="utf-8") as handle:
        for line_number, line in enumerate(handle, 1):
            if not line.strip():
                continue
            try:
                records.append(json.loads(line))
            except json.JSONDecodeError as error:
                raise ValueError(f"{path}:{line_number}: {error}") from error
    return records


def safe_div(numerator: float, denominator: float) -> float:
    return numerator / denominator if denominator else 0.0


def f1(precision: float, recall: float) -> float:
    return safe_div(2 * precision * recall, precision + recall)


def multilabel_metrics(
    gold_sets: list[set[str]], prediction_sets: list[set[str]], labels: Iterable[str]
) -> dict[str, Any]:
    label_metrics: dict[str, dict[str, float | int]] = {}
    total_tp = total_fp = total_fn = 0
    for label in sorted(set(labels)):
        tp = sum(label in gold and label in pred for gold, pred in zip(gold_sets, prediction_sets))
        fp = sum(label not in gold and label in pred for gold, pred in zip(gold_sets, prediction_sets))
        fn = sum(label in gold and label not in pred for gold, pred in zip(gold_sets, prediction_sets))
        precision = safe_div(tp, tp + fp)
        recall = safe_div(tp, tp + fn)
        label_metrics[label] = {
            "support": tp + fn,
            "precision": precision,
            "recall": recall,
            "f1": f1(precision, recall),
            "falsePositiveRatePerDocument": safe_div(fp, len(gold_sets)),
        }
        total_tp += tp
        total_fp += fp
        total_fn += fn

    micro_precision = safe_div(total_tp, total_tp + total_fp)
    micro_recall = safe_div(total_tp, total_tp + total_fn)
    supported = [metric for metric in label_metrics.values() if metric["support"]]
    return {
        "documents": len(gold_sets),
        "microPrecision": micro_precision,
        "microRecall": micro_recall,
        "microF1": f1(micro_precision, micro_recall),
        "macroPrecision": safe_div(sum(float(m["precision"]) for m in supported), len(supported)),
        "macroRecall": safe_div(sum(float(m["recall"]) for m in supported), len(supported)),
        "macroF1": safe_div(sum(float(m["f1"]) for m in supported), len(supported)),
        "exactMatchRatio": safe_div(
            sum(gold == pred for gold, pred in zip(gold_sets, prediction_sets)), len(gold_sets)
        ),
        "byLabel": label_metrics,
    }


def categorical_metrics(gold: list[str], predicted: list[str]) -> dict[str, Any]:
    labels = sorted(set(gold) | set(predicted))
    by_label: dict[str, dict[str, float | int]] = {}
    for label in labels:
        tp = sum(g == label and p == label for g, p in zip(gold, predicted))
        fp = sum(g != label and p == label for g, p in zip(gold, predicted))
        fn = sum(g == label and p != label for g, p in zip(gold, predicted))
        precision = safe_div(tp, tp + fp)
        recall = safe_div(tp, tp + fn)
        by_label[label] = {
            "support": sum(g == label for g in gold),
            "precision": precision,
            "recall": recall,
            "f1": f1(precision, recall),
        }
    return {
        "accuracy": safe_div(sum(g == p for g, p in zip(gold, predicted)), len(gold)),
        "byLabel": by_label,
    }


def parse_instant(value: str | None) -> datetime | None:
    if not value:
        return None
    return datetime.fromisoformat(value.replace("Z", "+00:00"))


def deadline_metrics(gold: list[str | None], predicted: list[str | None]) -> dict[str, float | int]:
    exact = within_day = wrong_presence = 0
    comparable = 0
    for gold_value, predicted_value in zip(gold, predicted):
        if bool(gold_value) != bool(predicted_value):
            wrong_presence += 1
        if not gold_value or not predicted_value:
            continue
        comparable += 1
        gold_dt = parse_instant(gold_value)
        predicted_dt = parse_instant(predicted_value)
        if gold_dt == predicted_dt:
            exact += 1
        if gold_dt and predicted_dt and abs((gold_dt - predicted_dt).total_seconds()) <= 86_400:
            within_day += 1
    return {
        "documents": len(gold),
        "comparableDeadlines": comparable,
        "exactMatchRatio": safe_div(exact, comparable),
        "within24HoursRatio": safe_div(within_day, comparable),
        "presenceAccuracy": 1 - safe_div(wrong_presence, len(gold)),
    }


def load_catalog(path: Path) -> tuple[set[str], dict[str, str | None]]:
    catalog = json.loads(path.read_text(encoding="utf-8"))
    topic_ids = {topic["topicId"] for topic in catalog["topics"]}
    parents = {topic["topicId"]: topic.get("parentTopicId") for topic in catalog["topics"]}
    return topic_ids, parents


def expand(topic_ids: set[str], parents: dict[str, str | None]) -> set[str]:
    result: set[str] = set()
    for topic_id in topic_ids:
        current: str | None = topic_id
        while current and current not in result:
            result.add(current)
            current = parents.get(current)
    return result


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--gold", required=True, type=Path)
    parser.add_argument("--predictions", required=True, type=Path)
    parser.add_argument("--catalog", required=True, type=Path)
    parser.add_argument("--output", type=Path)
    parser.add_argument("--min-micro-f1", type=float, default=0.0)
    parser.add_argument("--min-exact-match", type=float, default=0.0)
    args = parser.parse_args()

    gold_records = read_jsonl(args.gold)
    predictions_by_id = {record["noticeId"]: record for record in read_jsonl(args.predictions)}
    missing = [record["noticeId"] for record in gold_records if record["noticeId"] not in predictions_by_id]
    if missing:
        raise ValueError(f"Missing predictions for {len(missing)} notices: {missing[:10]}")

    topic_ids, parents = load_catalog(args.catalog)
    gold_direct = [set(record["directTopicIds"]) for record in gold_records]
    predicted_direct = [
        set(predictions_by_id[record["noticeId"]].get("directTopicIds", [])) for record in gold_records
    ]
    unknown_predictions = sorted(set.union(set(), *predicted_direct) - topic_ids)
    if unknown_predictions:
        raise ValueError(f"Unknown predicted Topic IDs: {unknown_predictions}")

    direct = multilabel_metrics(gold_direct, predicted_direct, topic_ids)
    hierarchical = multilabel_metrics(
        [expand(values, parents) for values in gold_direct],
        [expand(values, parents) for values in predicted_direct],
        topic_ids,
    )
    event = categorical_metrics(
        [record["eventType"] for record in gold_records],
        [predictions_by_id[record["noticeId"]].get("eventType", "UNKNOWN") for record in gold_records],
    )
    audience_labels = {"ALL", "ENROLLED_STUDENT", "NEW_STUDENT", "TRANSFER_STUDENT",
                       "GRADUATION_CANDIDATE", "GRADUATE"}
    campus_labels = {"ALL", "SEOUL", "YONGIN"}
    audiences = multilabel_metrics(
        [set(record["audiences"]) for record in gold_records],
        [set(predictions_by_id[record["noticeId"]].get("audiences", [])) for record in gold_records],
        audience_labels,
    )
    campuses = multilabel_metrics(
        [set(record["campuses"]) for record in gold_records],
        [set(predictions_by_id[record["noticeId"]].get("campuses", [])) for record in gold_records],
        campus_labels,
    )
    deadlines = deadline_metrics(
        [record.get("deadline") for record in gold_records],
        [predictions_by_id[record["noticeId"]].get("deadline") for record in gold_records],
    )
    report = {
        "directTopics": direct,
        "hierarchicalTopics": hierarchical,
        "eventType": event,
        "audiences": audiences,
        "campuses": campuses,
        "deadline": deadlines,
    }

    rendered = json.dumps(report, ensure_ascii=False, indent=2)
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(rendered + "\n", encoding="utf-8")

    print(f"documents={len(gold_records)}")
    print(f"direct_topic_micro_f1={direct['microF1']:.4f}")
    print(f"direct_topic_macro_f1={direct['macroF1']:.4f}")
    print(f"direct_topic_exact_match={direct['exactMatchRatio']:.4f}")
    print(f"hierarchical_topic_f1={hierarchical['microF1']:.4f}")
    print(f"event_accuracy={event['accuracy']:.4f}")
    print(f"audience_micro_f1={audiences['microF1']:.4f}")
    print(f"campus_micro_f1={campuses['microF1']:.4f}")
    print(f"deadline_presence_accuracy={deadlines['presenceAccuracy']:.4f}")

    passed = (
        direct["microF1"] >= args.min_micro_f1
        and direct["exactMatchRatio"] >= args.min_exact_match
        and not math.isnan(direct["microF1"])
    )
    return 0 if passed else 2


if __name__ == "__main__":
    raise SystemExit(main())
