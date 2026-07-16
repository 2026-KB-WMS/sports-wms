package com.example.sportswms.global.exception.warehouse;

import com.example.sportswms.global.util.MessageUtils;
import org.springframework.http.HttpStatus;

/** 창고/구역/사용자를 찾을 수 없음 → 404 Not Found */
public class WarehouseNotFoundException extends WarehouseException {
    private WarehouseNotFoundException(String message) {
        super(message);
    }

    public static WarehouseNotFoundException warehouse() {
        return new WarehouseNotFoundException(MessageUtils.getMessage("warehouseId.invalid"));
    }

    public static WarehouseNotFoundException user() {
        return new WarehouseNotFoundException(MessageUtils.getMessage("userId.invalid"));
    }

    public static WarehouseNotFoundException section() {
        return new WarehouseNotFoundException(MessageUtils.getMessage("section.id.invalid"));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
