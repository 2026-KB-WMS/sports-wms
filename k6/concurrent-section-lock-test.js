import http from 'k6/http';
import { check } from 'k6';

/**
 * 동시 다중 관리자 -> 같은 Section에 대한 입고 구역 배정 동시성 테스트
 * 대상: PATCH /api/inbounds/{inboundId}/details/{detailId}/section?sectionId={sectionId}
 *
 * 목적: 여러 창고관리자(혹은 동일 계정의 여러 세션)가 "정확히 같은 시각"에
 *       같은 Section(재고 구역)에 쓰기를 시도했을 때, Section 엔티티의 @Version
 *       낙관적 락이 의도대로 동작해서 1건만 성공(200)하고 나머지는 409로
 *       떨어지는지 확인한다.
 *
 * 실행 전 준비 (직접 값 채워야 함):
 *   1. 테스트용 Inbound 1건과 그 안의 InboundDetail 1건의 ID를 확보
 *      (관리자 페이지 또는 GET /api/inbounds, GET /api/inbounds/{id}/details 로 조회)
 *   2. 해당 Detail에 배정 가능한 Section ID 확보
 *      (GET /api/inbounds/{id}/assignable-sections)
 *   3. 아래 INBOUND_ID / DETAIL_ID / SECTION_ID 를 실행 시 -e 로 넘기거나 기본값 수정
 *
 * 실행 예:
 *   k6 run \
 *     -e INBOUND_ID=1 -e DETAIL_ID=1 -e SECTION_ID=1 \
 *     -e CONCURRENT_USERS=10 \
 *     k6/concurrent-section-lock-test.js
 *
 * 계정 풀을 바꾸고 싶으면 CREDENTIALS 배열을 수정 (창고관리자를 같은 창고에
 * 여러 명 배정해둔 상태라면 실제 admin/wm1/wm2... 로 나눠서 넣으면 더 현실적인 테스트가 됨)
 */

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const INBOUND_ID = __ENV.INBOUND_ID || '1';
const DETAIL_ID = __ENV.DETAIL_ID || '1';
const SECTION_ID = __ENV.SECTION_ID || '1';
const CONCURRENT_USERS = parseInt(__ENV.CONCURRENT_USERS || '10', 10);

// 동시에 요청을 보낼 계정 풀. 기본은 admin 반복이지만,
// 실제 여러 창고관리자를 같은 창고에 배정해뒀다면 여기를 ['wm1','wm2','wm3',...] 로 바꾸면 됨
const CREDENTIALS = (__ENV.CREDENTIALS
  ? __ENV.CREDENTIALS.split(',')
  : ['admin']
).map((loginId) => ({ loginId, password: __ENV.PASSWORD || 'Test1234!' }));

export const options = {
  scenarios: {
    // per-vu-iterations + 짧은 maxDuration 으로 모든 VU가 거의 동시에 1회씩 요청을 쏘게 함
    concurrent_write: {
      executor: 'per-vu-iterations',
      vus: CONCURRENT_USERS,
      iterations: 1,
      maxDuration: '30s',
    },
  },
};

export default function () {
  const cred = CREDENTIALS[__VU % CREDENTIALS.length];

  // 각 VU 로그인 (독립 쿠키 저장소이므로 VU별로 세션 분리됨)
  const loginRes = http.post(
    `${BASE_URL}/login`,
    { loginId: cred.loginId, password: cred.password },
    {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      tags: { name: 'login' },
    }
  );

  check(loginRes, { 'login status is 200': (r) => r.status === 200 });

  // 같은 Section에 동시 쓰기 시도
  const res = http.patch(
    `${BASE_URL}/api/inbounds/${INBOUND_ID}/details/${DETAIL_ID}/section?sectionId=${SECTION_ID}`,
    null,
    { tags: { name: 'assign_section_conflict' } }
  );

  // 기대: 여러 VU 중 정확히 1건만 200, 나머지는 409 (낙관적 락 충돌)
  // 또는 이미 배정된 상태라면 도메인 규칙에 따라 400/409 등이 나올 수 있음 -> 로그로 실제 분포 확인
  check(res, {
    'success or conflict (not 500)': (r) => r.status !== 500,
  });

  console.log(`VU ${__VU} (${cred.loginId}) -> status ${res.status}`);
}
