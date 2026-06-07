package com.example.sportswms.domain.order.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoreManagementType {
    OWNER("지점 점주"),
    STORE_MANAGER("지점 매니저");

    private final String title;
}
