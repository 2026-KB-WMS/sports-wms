package com.example.sportswms.domain.inbound.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum InboundStatus {

    PENDING("대기 중"),
    RECEIVED("접수 완료"),
    DELIVERING("배송 중"),
    INSPECTING("검수 중"),
    COMPLETED("입고 완료");

    private final String description;
}
