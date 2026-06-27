package com.example.sportswms.domain.inventory.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TransactionType {

    // 입고 관련 (Inbound)
    INBOUND_RECEIPT("입고 등록", "물류 센터에 재고가 도착하여 검수 후 임시 구역에 재고 등록", "PLUS"),
    STACKING_COMPLETE("적치 완료", "임시 구역에서 실제 구역으로 이동 후 최종 가용 재고 등록", "PLUS"),
    RETURN_RECEIPT("반품 입고", "반품 재고가 창고 반품 구역으로 입고되어 재고 등록", "PLUS"),

    // 출고 관련 (Outbound)
    ALLOCATE("출고 할당", "주문 접수로 인한 재고 할당", "NONE"),
    ALLOCATE_CANCEL("출고 할당 취소", "주문 취소 등으로 인한 재고 할당 취소", "NONE"),
    SHIPMENT_COMPLETE("출고 완료", "작업자 피킹 후 상차 완료 및 재고 차감", "MINUS"),

    // 이동 관련 (Movement)
    MOVE_OUT("구역 반출", "파손 발견, 로케이션 재배치로 인한 재고 반출", "MINUS"),
    MOVE_IN("구역 반입", "반출된 재고가 목적지에 최종 입고", "PLUS"),

    // 불량 관련 (Defect)
    DEFECT_INBOUND("불량 입고 등록", "입고 검수 중 불량품이 발견되어 불량 구역으로 격리", "PLUS"),

    // 조정 관련 (Adjustment)
    STOCK_ADJUSTMENT_PLUS("재고 조정(증가)", "재고 실사 중 장부보다 실물이 더 많은 것이 발견되어 강제 증량", "PLUS"),
    STOCK_ADJUSTMENT_MINUS("재고 조정(감소)", "재고 실사 중 분실, 도난 등이 확인되어 장부 재고를 강제 감량", "MINUS");

    private final String title;       // 한글 명칭 (화면 표시용)
    private final String description; // 상세 설명
    private final String effect;      // 실재고에 미치는 영향 (PLUS / MINUS / NONE)
}
