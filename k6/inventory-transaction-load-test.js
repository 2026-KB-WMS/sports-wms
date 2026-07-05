import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * InventoryTransaction 대용량 조회 부하테스트
 * 대상: GET /api/inventory/transactions
 *
 * 목적: 페이지네이션 없이 100만 건을 전체 조회할 때 응답시간/메모리 압박이
 *       얼마나 발생하는지 측정 (병목 재현 및 Pageable 도입 전/후 비교용)
 *
 * 시나리오:
 *   1. 파라미터 없이 전체 조회 (최악의 케이스 — 100만 건 전부 로드)
 *   2. warehouseId 지정 조회 (필터링 케이스 — 데이터 범위 축소)
 *
 * 실행:
 *   # 전체 조회 (최악의 케이스)
 *   k6 run k6/inventory-transaction-load-test.js
 *
 *   # 특정 창고로 필터링
 *   k6 run -e WAREHOUSE_ID=1 k6/inventory-transaction-load-test.js
 *
 *   # VU/반복 수 조정
 *   k6 run -e VUS=5 -e ITERATIONS=3 k6/inventory-transaction-load-test.js
 */

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const WAREHOUSE_ID = __ENV.WAREHOUSE_ID || '';
const VUS = parseInt(__ENV.VUS || '5', 10);
const ITERATIONS = parseInt(__ENV.ITERATIONS || '3', 10);

export const options = {
  scenarios: {
    transaction_load: {
      executor: 'per-vu-iterations',
      vus: VUS,
      iterations: ITERATIONS,
      maxDuration: '5m', // 100만 건 조회는 응답 자체가 오래 걸릴 수 있음
    },
  },
  thresholds: {
    // 의도적으로 느슨하게 설정 — 병목을 "통과"시키는 게 아니라 "측정"하는 게 목적
    http_req_duration: ['p(95)<30000'],
    http_req_failed: ['rate<0.01'],
  },
};

function login() {
  const res = http.post(
    `${BASE_URL}/login`,
    { loginId: 'admin', password: 'Test1234!' },
    { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
  );
  check(res, { 'login 200': (r) => r.status === 200 });

  const setCookie = res.headers['Set-Cookie'] || res.headers['set-cookie'] || '';
  const match = setCookie.match(/JSESSIONID=([^;]+)/);
  if (match) {
    http.cookieJar().set(BASE_URL, 'JSESSIONID', match[1]);
  } else {
    console.error('JSESSIONID not found in login response');
  }
}

export default function () {
  const jar = http.cookieJar();
  const cookies = jar.cookiesForURL(`${BASE_URL}/`);
  if (!cookies['JSESSIONID']) {
    login();
  }

  const PAGE = __ENV.PAGE || '0';
  const SIZE = __ENV.SIZE || '50';
  let query = `?page=${PAGE}&size=${SIZE}`;
  if (WAREHOUSE_ID) query += `&warehouseId=${WAREHOUSE_ID}`;
  const url = `${BASE_URL}/api/inventory/transactions${query}`;

  const start = Date.now();
  const res = http.get(url, { tags: { name: 'get_transactions' } });
  const elapsed = Date.now() - start;

  check(res, {
    'status 200': (r) => r.status === 200,
    'not unauthorized': (r) => r.status !== 401,
  });

  // 응답 크기와 시간을 직접 로그로 확인
  console.log(
    `[VU${__VU}] status=${res.status} duration=${elapsed}ms size=${(res.body ? res.body.length / 1024 : 0).toFixed(1)}KB`
  );

  sleep(1);
}
