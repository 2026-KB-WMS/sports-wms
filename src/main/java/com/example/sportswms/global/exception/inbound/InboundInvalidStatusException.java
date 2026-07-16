package com.example.sportswms.global.exception.inbound;

import com.example.sportswms.global.util.MessageUtils;
import org.springframework.http.HttpStatus;

/** 현재 입고 상태에서 허용되지 않는 작업 → 409 Conflict */
public class InboundInvalidStatusException extends InboundException {
    private InboundInvalidStatusException(String message) {
        super(message);
    }

    public static InboundInvalidStatusException notInspecting() {
        return new InboundInvalidStatusException(MessageUtils.getMessage("inbound.status.not.allowed"));
    }

    public static InboundInvalidStatusException defectNotRecorded() {
        return new InboundInvalidStatusException(MessageUtils.getMessage("inbound.defect.assign.after.recorded"));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}
