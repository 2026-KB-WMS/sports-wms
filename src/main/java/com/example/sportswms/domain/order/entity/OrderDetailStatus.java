package com.example.sportswms.domain.order.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OrderDetailStatus {

    PENDING("대기 중"),       // 지점이 발주했지만 아직 창고에 배정되지 않은 상태
    ASSIGNED("창고 배정"),    // 본사관리자가 창고에 위임 (Outbound 생성됨)
    DELIVERING("배송 중"),    // 출고가 SHIPPED 상태 (배송 출발)
    COMPLETED("완료"),        // 출고가 DELIVERED (지점 수령 완료)
    CANCELED("취소");

    private final String description;
}
