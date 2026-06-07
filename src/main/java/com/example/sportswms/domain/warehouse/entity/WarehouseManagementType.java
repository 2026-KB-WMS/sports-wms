package com.example.sportswms.domain.warehouse.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum WarehouseManagementType {
    MASTER("창고 총괄 책임자", "창고 내 모든 권한 소유 및 구역/로케이션 수정 가능"),
    INBOUND_WORKER("입고 담당자", "입고 검수, 적치 작업 및 입고 확정 처리 가능"),
    OUTBOUND_WORKER("출고 담당자", "주문 취합, 피킹, 패킹 및 상하차 출고 확정 처리 가능"),
    INVENTORY_MANAGER("재고 관리자", "재고 실사, 로케이션 이동 및 불량 재고 보류 처리 가능");

    private final String roleName;
    private final String description;
}
