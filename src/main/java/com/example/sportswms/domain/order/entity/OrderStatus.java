package com.example.sportswms.domain.order.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OrderStatus {

    PENDING("대기 중"),
    IN_PROGRESS("처리 중"),
    COMPLETED("처리 완료"),
    DENIED("발주 거부");

    private final String description;
}
