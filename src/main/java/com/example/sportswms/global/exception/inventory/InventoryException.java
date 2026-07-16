package com.example.sportswms.global.exception.inventory;

import com.example.sportswms.global.exception.BusinessException;

/** 재고(Inventory) 도메인 비즈니스 예외의 상위 클래스 */
public abstract class InventoryException extends BusinessException {
    protected InventoryException(String message) {
        super(message);
    }
}
