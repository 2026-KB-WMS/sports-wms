package com.example.sportswms.global.exception.inbound;

import com.example.sportswms.global.util.MessageUtils;
import org.springframework.http.HttpStatus;

/** 입고/입고상세/구역/창고/SKU를 찾을 수 없음 → 404 Not Found */
public class InboundNotFoundException extends InboundException {
    private InboundNotFoundException(String message) {
        super(message);
    }

    public static InboundNotFoundException inbound() {
        return new InboundNotFoundException(MessageUtils.getMessage("inbound.invalid"));
    }

    public static InboundNotFoundException detail() {
        return new InboundNotFoundException(MessageUtils.getMessage("inbound.detail.invalid"));
    }

    public static InboundNotFoundException section() {
        return new InboundNotFoundException(MessageUtils.getMessage("sectionId.invalid"));
    }

    public static InboundNotFoundException warehouse() {
        return new InboundNotFoundException(MessageUtils.getMessage("warehouseId.invalid"));
    }

    public static InboundNotFoundException sku() {
        return new InboundNotFoundException(MessageUtils.getMessage("sku.invalid"));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
