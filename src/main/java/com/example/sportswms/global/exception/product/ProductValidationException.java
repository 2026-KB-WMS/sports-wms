package com.example.sportswms.global.exception.product;

import com.example.sportswms.global.util.MessageUtils;

/** 스펙/옵션 조합 등 상품 비즈니스 규칙 위반 → 400 Bad Request */
public class ProductValidationException extends ProductException {
    private ProductValidationException(String message) {
        super(message);
    }

    public static ProductValidationException specIncomplete() {
        return new ProductValidationException(MessageUtils.getMessage("product.spec.incomplete"));
    }

    public static ProductValidationException optionInvalid() {
        return new ProductValidationException(MessageUtils.getMessage("option.invalid"));
    }

    public static ProductValidationException skuOptionGroupIncomplete() {
        return new ProductValidationException(MessageUtils.getMessage("sku.option.group.incomplete"));
    }
}
