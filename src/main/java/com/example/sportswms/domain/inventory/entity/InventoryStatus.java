package com.example.sportswms.domain.inventory.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum InventoryStatus {

    UNALLOCATED("미할당"),
    ALLOCATED("할당"),
    NORMAL("정상"),
    DEFECTIVE("파손/불량");

    private final String description;
}
