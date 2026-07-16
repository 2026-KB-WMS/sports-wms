package com.example.sportswms.global.exception.outbound;

import com.example.sportswms.global.util.MessageUtils;
import org.springframework.http.HttpStatus;

/** 출고/출고상세/구역을 찾을 수 없음 → 404 Not Found */
public class OutboundNotFoundException extends OutboundException {
    private OutboundNotFoundException(String message) {
        super(message);
    }

    public static OutboundNotFoundException outbound() {
        return new OutboundNotFoundException(MessageUtils.getMessage("outbound.invalid"));
    }

    public static OutboundNotFoundException detail() {
        return new OutboundNotFoundException(MessageUtils.getMessage("outbound.detail.invalid"));
    }

    public static OutboundNotFoundException section() {
        return new OutboundNotFoundException(MessageUtils.getMessage("sectionId.invalid"));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
