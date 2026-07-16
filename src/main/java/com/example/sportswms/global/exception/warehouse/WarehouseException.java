package com.example.sportswms.global.exception.warehouse;

import com.example.sportswms.global.exception.BusinessException;

/** 창고/구역(Warehouse, Section) 도메인 비즈니스 예외의 상위 클래스 */
public abstract class WarehouseException extends BusinessException {
    protected WarehouseException(String message) {
        super(message);
    }
}
