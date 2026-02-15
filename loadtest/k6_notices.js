console.log(`[INIT] BASE_URL=${BASE_URL} NOTICE_CATEGORY=${NOTICE_CATEGORY} NOTICE_MAX_PAGE=${NOTICE_MAX_PAGE} NOTICE_YEAR=${NOTICE_YEAR}`);

import http from "k6/http";
import { check, sleep, group } from "k6";
import { Trend, Counter, Rate } from "k6/metrics";

/**
 * =========================
 * Environment Config
 * =========================
 */

const BASE_URL = __ENV.BASE_URL || "http://172.17.0.1:8080";
const NOTICE_MAX_PAGE = parseInt(__ENV.NOTICE_MAX_PAGE || "20", 10);
const NOTICE_SIZE = parseInt(__ENV.NOTICE_SIZE || "15", 10);
const NOTICE_SORT = __ENV.NOTICE_SORT || "desc";
const NOTICE_CATEGORY = __ENV.NOTICE_CATEGORY;
const NOTICE_YEAR = __ENV.NOTICE_YEAR;

/**
 * =========================
 * Custom Metrics
 * =========================
 */

const noticeListLatency = new Trend("notice_list_latency_ms");
const noticeListOK = new Rate("notice_list_ok_rate");
const noticeListFail = new Counter("notice_list_fail_count");

/**
 * =========================
 * k6 Options
 * =========================
 */

export const options = {
    scenarios: {
        notice_list: {
            executor: "ramping-vus",
            startVUs: 0,
            stages: [
                { duration: "30s", target: 50 },
                { duration: "1m", target: 500 },
                { duration: "2m", target: 500 },  // 유지 구간
                { duration: "30s", target: 0 },
            ],

            gracefulRampDown: "30s",
            exec: "noticeListScenario",
            tags: { test_type: "notice_list" },
        },
    },

    thresholds: {
        http_req_failed: ["rate<0.01"],
        http_req_duration: ["p(95)<500"],

        notice_list_ok_rate: ["rate>0.99"],
        notice_list_latency_ms: ["p(95)<500"],
    },

    // URL 라벨 폭발 방지
    systemTags: ["status", "method", "name"],
};

/**
 * =========================
 * Helper Functions
 * =========================
 */

function buildQuery(params) {
    const entries = Object.entries(params)
        .filter(([_, v]) => v !== undefined && v !== null && v !== "");
    if (entries.length === 0) return "";
    return (
        "?" +
        entries
            .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
            .join("&")
    );
}

function randomInt(min, maxExclusive) {
    return Math.floor(Math.random() * (maxExclusive - min)) + min;
}

/**
 * =========================
 * Scenario
 * =========================
 */

export function noticeListScenario() {
    group("notice_list", () => {

        const page = randomInt(0, Math.max(1, NOTICE_MAX_PAGE));

        const qs = buildQuery({
            page,
            size: NOTICE_SIZE,
            sort: NOTICE_SORT,
            category: NOTICE_CATEGORY,
            year: NOTICE_YEAR,
        });

        const url = `${BASE_URL}/api/v1/notices${qs}`;

        const res = http.get(url, {
            tags: {
                name: "GET /api/v1/notices",
                endpoint: "notice_list",
            },
        });

        const ok = check(res, {
            "status is 200": (r) => r.status === 200,
            "content-type is json": (r) =>
                (r.headers["Content-Type"] || "").includes("application/json"),
        });

        noticeListOK.add(ok);
        noticeListLatency.add(res.timings.duration);

        if (!ok) {
            noticeListFail.add(1);
        }

        sleep(0.05);
    });
}
