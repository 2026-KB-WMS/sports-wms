package com.example.sportswms.domain.outbound.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OutboundStatus {
    PENDING("출고 요청"),      // 가맹점(점주)의 발주 및 출고 대기 상태
    ASSIGNED("창고 배정"),     // 본사 관리자가 발주 상세를 창고에 위임한 직후 (Outbound 생성 시 초기 상태). 재고 확인/구역 배정 전.
    APPROVED("출고 승인"),     // 창고 관리자가 가용 재고 확인 후 모든 품목에 구역(피킹 위치)을 배정 완료한 상태
    PICKING("피킹 중"),        // 창고 작업자가 지시서를 토대로 실물 물품을 집책하는 단계
    PACKING("검수 및 포장"),   // 오배송 방지 바코드 스캔 및 합포장 작업 단계
    SHIPPED("배송 중"),        // 창고 출고 도크 출발, 실물 재고 차감 시점
    DELIVERED("배송 완료"),    // 가맹점 점주가 물품을 최종 수령 완료한 상태
    CANCELED("출고 취소");     // 승인 전 혹은 작업 전 출고가 취소된 상태

    private final String description;
}
