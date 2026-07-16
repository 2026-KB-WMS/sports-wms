package com.example.sportswms.global.exception.inventory;

import com.example.sportswms.global.util.MessageUtils;
import org.springframework.http.HttpStatus;

/** 재고 레코드를 찾을 수 없음 → 404 Not Found */
public class InventoryNotFoundException extends InventoryException {
    public InventoryNotFoundException() {
        super(MessageUtils.getMessage("inventory.not.found"));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
