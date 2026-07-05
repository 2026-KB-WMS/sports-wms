import http from 'k6/http';
import { check, sleep } from 'k6';
import exec from 'k6/execution';

/**
 * SportsWMS 재고 조회 API 부하테스트
 * 대상: GET /api/inventory (N+1 이슈 있었던 구간, JOIN FETCH 적용 후 성능 비교용)
 *
 * 인증: session-login-test.js와 동일한 formLogin 방식 (SecurityConfig.java 기준)
 * 권한: /api/inventory/** 는 GENERAL_MANAGER, WAREHOUSE_MANAGER 만 접근 가능
 *       -> 창고관리자 계정(wm1)으로 테스트하면 warehouseId 스코프 검증(AccessValidator)까지 같이 확인 가능
 */

export const options = {
  scenarios: {
    default: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '20s', target: 200 },
        { duration: '20s', target: 400 },
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
// 특정 창고로 좁혀서 조회하고 싶으면 실행 시 -e WAREHOUSE_ID=1 로 지정
const WAREHOUSE_ID = __ENV.WAREHOUSE_ID || '';

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
  if (exec.vu.iterationInInstance === 0) {
    login();
  }

  const query = WAREHOUSE_ID ? `?warehouseId=${WAREHOUSE_ID}` : '';
  const res = http.get(`${BASE_URL}/api/inventory${query}`, {
    tags: { name: 'get_inventory' },
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
    'not unauthorized': (r) => r.status !== 401,
    'not forbidden': (r) => r.status !== 403,
  });

  sleep(1);
}
