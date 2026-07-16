package com.example.sportswms.global.exception.outbound;

import com.example.sportswms.global.util.MessageUtils;
import org.springframework.http.HttpStatus;

/** 현재 출고 상태에서 허용되지 않는 작업 → 409 Conflict */
public class OutboundInvalidStatusException extends OutboundException {
    private OutboundInvalidStatusException(String message) {
        super(message);
    }

    public static OutboundInvalidStatusException statusNotAllowed() {
        return new OutboundInvalidStatusException(MessageUtils.getMessage("outbound.status.not.allowed"));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}
