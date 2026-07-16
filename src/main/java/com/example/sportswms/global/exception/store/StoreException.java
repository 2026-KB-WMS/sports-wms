package com.example.sportswms.global.exception.store;

import com.example.sportswms.global.exception.BusinessException;

/** 지점/발주(Store, StockOrder) 도메인 비즈니스 예외의 상위 클래스 */
public abstract class StoreException extends BusinessException {
    protected StoreException(String message) {
        super(message);
    }
}
