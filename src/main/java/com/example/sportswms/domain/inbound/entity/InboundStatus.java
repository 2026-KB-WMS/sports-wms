package com.example.sportswms.domain.inbound.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum InboundStatus {

    PENDING("대기 중"),
    IN_PROGRESS("진행 중"),
    COMPLETED("입고 완료");

    private final String description;
}
