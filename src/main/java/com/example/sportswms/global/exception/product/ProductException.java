package com.example.sportswms.global.exception.product;

import com.example.sportswms.global.exception.BusinessException;

/** 상품/브랜드/SKU 도메인 비즈니스 예외의 상위 클래스 */
public abstract class ProductException extends BusinessException {
    protected ProductException(String message) {
        super(message);
    }
}
