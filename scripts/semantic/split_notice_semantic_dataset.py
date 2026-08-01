#!/usr/bin/env python3
"""Create deterministic group-safe splits plus a time holdout."""

from __future__ import annotations

import argparse
import hashlib
import json
from collections import Counter, defaultdict
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


def read_jsonl(path: Path) -> list[dict[str, Any]]:
    with path.open(encoding="utf-8") as handle:
        return [json.loads(line) for line in handle if line.strip()]


def parse_instant(value: str) -> datetime:
    return datetime.fromisoformat(value.replace("Z", "+00:00"))


def bucket(group_id: str, train_ratio: int, validation_ratio: int) -> str:
    value = int(hashlib.sha256(group_id.encode("utf-8")).hexdigest()[:8], 16) % 100
    if value < train_ratio:
        return "TRAIN"
    if value < train_ratio + validation_ratio:
        return "VALIDATION"
    return "TEST"


def write_jsonl(path: Path, records: list[dict[str, Any]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="\n") as handle:
        for record in records:
            handle.write(json.dumps(record, ensure_ascii=False, separators=(",", ":")) + "\n")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("input", type=Path)
    parser.add_argument("output_dir", type=Path)
    parser.add_argument("--time-holdout-start", default="2026-06-01T00:00:00+09:00")
    parser.add_argument("--train-percent", type=int, default=70)
    parser.add_argument("--validation-percent", type=int, default=15)
    args = parser.parse_args()
    if args.train_percent + args.validation_percent >= 100:
        raise ValueError("train + validation must be less than 100")

    records = read_jsonl(args.input)
    groups: dict[str, list[dict[str, Any]]] = defaultdict(list)
    for record in records:
        groups[record["sourceGroupId"]].append(record)

    holdout_start = parse_instant(args.time_holdout_start)
    if holdout_start.tzinfo is None:
        holdout_start = holdout_start.replace(tzinfo=timezone.utc)

    split_by_group: dict[str, str] = {}
    for group_id, group_records in groups.items():
        latest = max(parse_instant(record["publishedAt"]) for record in group_records)
        if latest >= holdout_start:
            split_by_group[group_id] = "TIME_HOLDOUT"
        else:
            split_by_group[group_id] = bucket(
                group_id, args.train_percent, args.validation_percent
            )

    split_records: dict[str, list[dict[str, Any]]] = defaultdict(list)
    for record in records:
        updated = dict(record)
        updated["split"] = split_by_group[record["sourceGroupId"]]
        split_records[updated["split"]].append(updated)

    for split in ("TRAIN", "VALIDATION", "TEST", "TIME_HOLDOUT"):
        write_jsonl(args.output_dir / f"{split.lower()}.jsonl", split_records[split])

    manifest = {
        "source": str(args.input),
        "timeHoldoutStart": args.time_holdout_start,
        "records": len(records),
        "groups": len(groups),
        "recordCounts": dict(Counter(record["split"] for values in split_records.values() for record in values)),
        "groupCounts": dict(Counter(split_by_group.values())),
    }
    (args.output_dir / "manifest.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print(json.dumps(manifest, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
