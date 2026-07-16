package com.example.sportswms.global.exception.store;

import com.example.sportswms.global.util.MessageUtils;
import org.springframework.http.HttpStatus;

/** 현재 발주 상태에서 허용되지 않는 작업(취소 불가 등) → 409 Conflict */
public class StoreInvalidStatusException extends StoreException {
    private StoreInvalidStatusException(String message) {
        super(message);
    }

    public static StoreInvalidStatusException cancelNotAllowed() {
        return new StoreInvalidStatusException(MessageUtils.getMessage("order.cancel.not.allowed"));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}
