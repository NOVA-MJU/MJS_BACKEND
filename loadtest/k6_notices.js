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
 * Load Config (ENV 기반)
 * =========================
 */

const START_VUS = parseInt(__ENV.START_VUS || "0", 10);
const WARMUP_VUS = parseInt(__ENV.WARMUP_VUS || "50", 10);
const PEAK_VUS = parseInt(__ENV.PEAK_VUS || "100", 10);

const WARMUP_DURATION = __ENV.WARMUP_DURATION || "30s";
const RAMP_DURATION = __ENV.RAMP_DURATION || "1m";
const HOLD_DURATION = __ENV.HOLD_DURATION || "2m";
const RAMPDOWN_DURATION = __ENV.RAMPDOWN_DURATION || "30s";

const SLEEP_SEC = parseFloat(__ENV.SLEEP_SEC || "0.05");

console.log(`
[INIT CONFIG]
BASE_URL=${BASE_URL}
NOTICE_SIZE=${NOTICE_SIZE}
NOTICE_MAX_PAGE=${NOTICE_MAX_PAGE}
NOTICE_CATEGORY=${NOTICE_CATEGORY}
START_VUS=${START_VUS}
PEAK_VUS=${PEAK_VUS}
HOLD_DURATION=${HOLD_DURATION}
SLEEP_SEC=${SLEEP_SEC}
`);

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
            startVUs: START_VUS,
            stages: [
                { duration: WARMUP_DURATION, target: WARMUP_VUS },
                { duration: RAMP_DURATION, target: PEAK_VUS },
                { duration: HOLD_DURATION, target: PEAK_VUS },
                { duration: RAMPDOWN_DURATION, target: 0 },
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

        sleep(SLEEP_SEC);
    });
}
