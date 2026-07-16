package com.example.sportswms.global.exception.store;

import com.example.sportswms.global.util.MessageUtils;
import org.springframework.http.HttpStatus;

/** 이미 다른 발주에 할당됨, 이미 배정된 담당자 등 → 409 Conflict */
public class StoreConflictException extends StoreException {
    private StoreConflictException(String message) {
        super(message);
    }

    public static StoreConflictException orderDetailAlreadyAssigned() {
        return new StoreConflictException(MessageUtils.getMessage("order.detail.assigned"));
    }

    public static StoreConflictException userAlreadyAssigned() {
        return new StoreConflictException(MessageUtils.getMessage("store.user.assigned"));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}
