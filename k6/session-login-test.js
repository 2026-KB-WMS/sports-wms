import http from 'k6/http';
import { check, sleep } from 'k6';
import exec from 'k6/execution';

/**
 * SportsWMS 세션 기반 로그인 + API 부하테스트
 *
 * 인증 방식: Spring Security formLogin (SecurityConfig.java 기준)
 *  - loginProcessingUrl: /login
 *  - usernameParameter : loginId (기본 파라미터명이 아니라 커스텀 지정됨)
 *  - passwordParameter : password (기본값 그대로 사용)
 *  - CSRF               : disable 되어 있음 -> 토큰 불필요
 *  - 성공 시 200, 실패 시 401 (리다이렉트 없음)
 *
 * 계정: DummyDataInitializer 기준 admin / Test1234! (ROLE_GENERAL_MANAGER)
 *
 * 동작 방식:
 *  - k6는 VU(가상유저)별로 독립된 쿠키 저장소(cookie jar)를 자동 관리한다.
 *  - 로그인 후 발급되는 JSESSIONID는 해당 VU의 이후 모든 요청에 자동으로 실려간다.
 *  - 따라서 "각 VU의 첫 iteration에서만 로그인"하도록 분기하면
 *    로그인 자체는 부하 측정에서 제외하고, 순수 API 성능만 측정할 수 있다.
 */

export const options = {
  scenarios: {
    default: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 20 },
        { duration: '1m', target: 20 },
        { duration: '10s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const LOGIN_ID = __ENV.LOGIN_ID || 'admin';
const PASSWORD = __ENV.PASSWORD || 'Test1234!';

function login() {
  const res = http.post(
    `${BASE_URL}/login`,
    {
      loginId: LOGIN_ID,
      password: PASSWORD,
    },
    {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      tags: { name: 'login' },
    }
  );

  check(res, {
    'login status is 200': (r) => r.status === 200,
  });

  return res;
}

export default function () {
  // 해당 VU의 첫 iteration에서만 로그인 수행 (이후 iteration은 세션 쿠키 재사용)
  if (exec.vu.iterationInInstance === 0) {
    login();
  }

  // ── 인증이 필요한 API 호출 ────────────────────────────────
  // 필요에 따라 엔드포인트를 교체해서 사용 (예: 재고 조회, 창고 조회 등)
  const res = http.get(`${BASE_URL}/api/warehouses`, {
    tags: { name: 'get_warehouses' },
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
    'not unauthorized': (r) => r.status !== 401,
  });

  sleep(1);
}
