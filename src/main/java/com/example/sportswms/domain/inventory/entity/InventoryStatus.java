package com.example.sportswms.domain.inventory.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum InventoryStatus {

    UNASSIGNED("미할당"),
    ASSIGNED("할당"),
    DAMAGED("파손됨");

    private final String description;
}
