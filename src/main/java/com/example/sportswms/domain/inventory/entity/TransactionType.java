package com.example.sportswms.domain.inventory.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TransactionType {

    INBOUND("입고"),
    OUTBOUND("출고"),
    WH_MOVE("창고이동");

    private final String description;
}
