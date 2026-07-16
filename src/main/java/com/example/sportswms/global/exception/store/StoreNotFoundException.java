package com.example.sportswms.global.exception.store;

import com.example.sportswms.global.util.MessageUtils;
import org.springframework.http.HttpStatus;

/** 발주/창고/지점/사용자를 찾을 수 없음 → 404 Not Found */
public class StoreNotFoundException extends StoreException {
    private StoreNotFoundException(String message) {
        super(message);
    }

    public static StoreNotFoundException order() {
        return new StoreNotFoundException(MessageUtils.getMessage("order.invalid"));
    }

    public static StoreNotFoundException warehouse() {
        return new StoreNotFoundException(MessageUtils.getMessage("warehouse.invalid"));
    }

    public static StoreNotFoundException store() {
        return new StoreNotFoundException(MessageUtils.getMessage("store.invalid"));
    }

    public static StoreNotFoundException user() {
        return new StoreNotFoundException(MessageUtils.getMessage("user.invalid"));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
