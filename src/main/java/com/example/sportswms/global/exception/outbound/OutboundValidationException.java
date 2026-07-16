package com.example.sportswms.global.exception.outbound;

import com.example.sportswms.global.util.MessageUtils;

/** 구역 배정 누락, 재고 부족 등 출고 비즈니스 규칙 위반 → 400 Bad Request */
public class OutboundValidationException extends OutboundException {
    private OutboundValidationException(String message) {
        super(message);
    }

    public static OutboundValidationException damagedSectionNotAllowed() {
        return new OutboundValidationException(MessageUtils.getMessage("outbound.section.damaged.not.allowed"));
    }

    public static OutboundValidationException sectionUnauthorized() {
        return new OutboundValidationException(MessageUtils.getMessage("outbound.section.unauthorized"));
    }

    public static OutboundValidationException sectionUnassigned() {
        return new OutboundValidationException(MessageUtils.getMessage("outbound.section.unassigned"));
    }

    public static OutboundValidationException inventoryInsufficient(int available, int required) {
        return new OutboundValidationException(
                MessageUtils.getMessage("outbound.inventory.insufficient", available, required));
    }
}
