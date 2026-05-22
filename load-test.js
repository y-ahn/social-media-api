// PART 7 — k6 부하 테스트 스크립트
// 설치: https://k6.io/docs/get-started/installation/
// 실행: k6 run load-test.js
// 대시보드: k6 run --out dashboard load-test.js

import http from "k6/http";
import { check, sleep, group } from "k6";
import { Counter, Rate, Trend } from "k6/metrics";

// ── 커스텀 메트릭 ─────────────────────────────────────────────────────
const loginSuccess   = new Counter("login_success");
const loginFail      = new Counter("login_fail");
const postCreateRate = new Rate("post_create_success_rate");
const feedDuration   = new Trend("feed_duration_ms");

// ── 부하 설정 ─────────────────────────────────────────────────────────
export const options = {
  stages: [
    { duration: "30s", target: 20  },  // Warm-up: 0 → 20 VU
    { duration: "1m",  target: 50  },  // 일반 부하: 50 VU 유지
    { duration: "30s", target: 100 },  // 피크 부하: 100 VU
    { duration: "30s", target: 0   },  // Cool-down
  ],
  thresholds: {
    http_req_duration:      ["p(95)<500"],  // 95% 요청 500ms 이내
    http_req_failed:        ["rate<0.01"],  // 에러율 1% 미만
    feed_duration_ms:       ["p(90)<300"],  // 피드 조회 90% 300ms 이내
    post_create_success_rate: ["rate>0.99"],
  },
};

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

// ── 테스트 데이터 ─────────────────────────────────────────────────────
const TEST_USERS = [
  { email: "user1@test.com", password: "Password1!" },
  { email: "user2@test.com", password: "Password1!" },
  { email: "user3@test.com", password: "Password1!" },
];

// ── 메인 시나리오 ─────────────────────────────────────────────────────
export default function () {
  const user = TEST_USERS[Math.floor(Math.random() * TEST_USERS.length)];

  group("1. 로그인", () => {
    const res = http.post(`${BASE_URL}/api/v1/auth/login`,
      JSON.stringify({ email: user.email, password: user.password }),
      { headers: { "Content-Type": "application/json" } }
    );

    const ok = check(res, {
      "로그인 200 OK": (r) => r.status === 200,
      "accessToken 존재": (r) => JSON.parse(r.body).data?.accessToken !== undefined,
    });

    if (ok) {
      loginSuccess.add(1);
      const token = JSON.parse(res.body).data.accessToken;

      group("2. 피드 조회 (Cursor 페이지네이션)", () => {
        const start = Date.now();
        const feedRes = http.get(`${BASE_URL}/api/v1/feed?size=20`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        feedDuration.add(Date.now() - start);

        check(feedRes, {
          "피드 조회 200": (r) => r.status === 200,
          "배열 반환":     (r) => Array.isArray(JSON.parse(r.body).data),
        });
      });

      group("3. 게시글 검색", () => {
        const searchRes = http.get(
          `${BASE_URL}/api/v1/posts?keyword=테스트&size=10&cursorId=`,
          { headers: { Authorization: `Bearer ${token}` } }
        );
        check(searchRes, { "검색 200": (r) => r.status === 200 });
      });

      group("4. 게시글 작성", () => {
        const createRes = http.post(
          `${BASE_URL}/api/v1/posts`,
          JSON.stringify({ content: `부하 테스트 게시글 ${Date.now()}` }),
          { headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          }}
        );
        const created = check(createRes, { "게시글 작성 201": (r) => r.status === 201 });
        postCreateRate.add(created);

        // 작성한 게시글 좋아요
        if (created) {
          const postId = JSON.parse(createRes.body).data?.id;
          if (postId) {
            http.post(`${BASE_URL}/api/v1/posts/${postId}/like`, null, {
              headers: { Authorization: `Bearer ${token}` },
            });
          }
        }
      });

    } else {
      loginFail.add(1);
    }
  });

  sleep(1); // 실제 사용자처럼 1초 대기
}

// ── 결과 요약 출력 ────────────────────────────────────────────────────
export function handleSummary(data) {
  return {
    "load-test-result.json": JSON.stringify(data, null, 2),
    stdout: `
=== 부하 테스트 결과 ===
총 요청 수:       ${data.metrics.http_reqs.values.count}
평균 응답시간:    ${data.metrics.http_req_duration.values.avg.toFixed(0)}ms
p95 응답시간:     ${data.metrics.http_req_duration.values["p(95)"].toFixed(0)}ms
피드 조회 p90:    ${data.metrics.feed_duration_ms?.values["p(90)"]?.toFixed(0) || "N/A"}ms
에러율:           ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%
로그인 성공:      ${data.metrics.login_success?.values.count || 0}건
로그인 실패:      ${data.metrics.login_fail?.values.count || 0}건
`,
  };
}
