import http from 'k6/http';
import { check, sleep } from 'k6';
import exec from 'k6/execution';

/**
 * 여러 지점(Store) 동시 대량 발주 부하테스트
 * 대상: POST /api/orders  (StoreService.createStoreOrderRequest)
 *
 * 이 API는 Section/Inventory 락 경합은 없지만(단순 StockOrderDetail insert),
 * 대신 다음 두 가지가 대용량/동시 요청 시 병목 후보임:
 *   1. items 개수만큼 productSKURepository.findById()가 개별 쿼리로 나가는 N+1
 *      (createStoreOrderRequest 내부 items.stream().map(...) 부분)
 *   2. 여러 지점(VU)이 동시에 insert를 몰아칠 때의 순수 쓰기 처리량 / 커넥션 풀 경합
 *
 * 계정: DummyDataInitializer 기준 owner1~owner10 (각자 다른 지점 1개씩 담당, ROLE_USER)
 * 인증: 세션 기반 formLogin (SecurityConfig.java 기준, usernameParameter=loginId, CSRF disable)
 *
 * 실행 예:
 *   # 지점당 발주 1건(아이템 5개)씩, 10개 지점이 동시에
 *   k6 run k6/bulk-order-load-test.js
 *
 *   # 아이템 개수를 늘려서 N+1 효과를 더 극단적으로 보고 싶을 때
 *   k6 run -e ITEMS_PER_ORDER=20 -e CONCURRENT_STORES=10 k6/bulk-order-load-test.js
 *
 * 주의: SKU_ID_MIN/MAX는 더미데이터 기준 추정치입니다.
 *       실행 전 DB에서 확인 후 필요시 -e 로 조정하세요.
 *         SELECT MIN(sku_id), MAX(sku_id) FROM ProductSKU;
 */

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const PASSWORD = __ENV.PASSWORD || 'Test1234!';
const CONCURRENT_STORES = parseInt(__ENV.CONCURRENT_STORES || '10', 10); // owner1~ownerN
const ITEMS_PER_ORDER = parseInt(__ENV.ITEMS_PER_ORDER || '5', 10);
const SKU_ID_MIN = parseInt(__ENV.SKU_ID_MIN || '1', 10);
const SKU_ID_MAX = parseInt(__ENV.SKU_ID_MAX || '57', 10);
// 각 지점이 몇 번 반복해서 발주를 넣을지 (한 번의 부하 실행 동안)
const ORDERS_PER_STORE = parseInt(__ENV.ORDERS_PER_STORE || '5', 10);

export const options = {
  scenarios: {
    bulk_order: {
      executor: 'per-vu-iterations',
      vus: CONCURRENT_STORES,       // owner1 ~ ownerN, 지점당 VU 1개
      iterations: ORDERS_PER_STORE, // 지점당 반복 발주 횟수
      maxDuration: '2m',
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<1000'],
    http_req_failed: ['rate<0.01'],
  },
};

function loginId() {
  // VU 번호(1-indexed)를 owner 계정에 매핑
  return `owner${((__VU - 1) % CONCURRENT_STORES) + 1}`;
}

function login() {
  const res = http.post(
    `${BASE_URL}/login`,
    { loginId: loginId(), password: PASSWORD },
    {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      tags: { name: 'login' },
    }
  );

  const ok = check(res, { 'login status is 200': (r) => r.status === 200 });
  if (!ok) {
    console.error(
      `[${loginId()}] LOGIN FAILED status=${res.status} body=${res.body ? res.body.substring(0, 200) : ''}`
    );
  }

  // localhost 도메인 쿠키 매칭 문제를 우회하기 위해 세션 쿠키를 수동으로 jar에 등록
  const setCookie = res.headers['Set-Cookie'] || res.headers['set-cookie'] || '';
  const match = setCookie.match(/JSESSIONID=([^;]+)/);
  if (match) {
    const jar = http.cookieJar();
    jar.set(BASE_URL, 'JSESSIONID', match[1]);
  } else {
    console.error(`[${loginId()}] Set-Cookie header not found in login response`);
  }
}

function getMyStoreId() {
  const res = http.get(`${BASE_URL}/api/orders/stores/my`, {
    tags: { name: 'get_my_store' },
  });

  const contentType = res.headers['Content-Type'] || res.headers['content-type'] || '';
  if (res.status !== 200 || !contentType.includes('application/json')) {
    console.error(
      `[${loginId()}] GET stores/my UNEXPECTED status=${res.status} content-type=${contentType} body=${res.body ? res.body.substring(0, 300) : ''}`
    );
    throw new Error(`${loginId()}: /api/orders/stores/my 응답이 JSON이 아님 (인증 실패 가능성 높음)`);
  }

  const stores = res.json();
  if (!stores || stores.length === 0) {
    throw new Error(`${loginId()} 계정에 배정된 지점이 없습니다.`);
  }
  return stores[0].id ?? stores[0].storeId; // 실제 StoreDTO 필드명에 맞게 필요시 조정
}

function randomSkuId() {
  return SKU_ID_MIN + Math.floor(Math.random() * (SKU_ID_MAX - SKU_ID_MIN + 1));
}

function buildItems() {
  const items = [];
  for (let i = 0; i < ITEMS_PER_ORDER; i++) {
    items.push({
      skuId: randomSkuId(),
      quantity: 1 + Math.floor(Math.random() * 10),
      memo: `k6 bulk order item ${i}`,
    });
  }
  return items;
}

export default function () {
  // per-vu-iterations에서 jar는 iteration 간 공유되지 않을 수 있으므로
  // 매 iteration마다 jar에 JSESSIONID가 있는지 확인하고 없으면 재로그인
  const jar = http.cookieJar();
  const cookies = jar.cookiesForURL(`${BASE_URL}/`);
  if (!cookies['JSESSIONID']) {
    login();
  }

  const storeId = getMyStoreId();

  const payload = JSON.stringify({
    storeId: storeId,
    items: buildItems(),
  });

  const res = http.post(`${BASE_URL}/api/orders`, payload, {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'submit_order' },
  });

  check(res, {
    'order created (201)': (r) => r.status === 201,
    'not unauthorized': (r) => r.status !== 401,
    'not forbidden': (r) => r.status !== 403,
  });

  if (res.status !== 201) {
    console.error(`[${loginId()}] order failed: ${res.status} ${res.body}`);
  }

  sleep(0.5);
}
