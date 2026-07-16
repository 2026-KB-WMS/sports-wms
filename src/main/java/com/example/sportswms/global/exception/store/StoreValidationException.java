package com.example.sportswms.global.exception.store;

import com.example.sportswms.global.util.MessageUtils;

/** 선택 항목 누락, 존재하지 않는 SKU 등 발주 비즈니스 규칙 위반 → 400 Bad Request */
public class StoreValidationException extends StoreException {
    private StoreValidationException(String message) {
        super(message);
    }

    public static StoreValidationException detailNotSelected() {
        return new StoreValidationException(MessageUtils.getMessage("order.detail.selected"));
    }

    public static StoreValidationException skuInvalid() {
        return new StoreValidationException(MessageUtils.getMessage("sku.invalid"));
    }
}
