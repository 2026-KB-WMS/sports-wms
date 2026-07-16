package com.example.sportswms.global.exception.inbound;

import com.example.sportswms.global.util.MessageUtils;

/** 구역 용량 초과, SKU 중복, 배정 누락 등 입고 비즈니스 규칙 위반 → 400 Bad Request */
public class InboundValidationException extends InboundException {
    private InboundValidationException(String message) {
        super(message);
    }

    public static InboundValidationException skuDuplicate() {
        return new InboundValidationException(MessageUtils.getMessage("inbound.sku.duplicate"));
    }

    public static InboundValidationException damagedSectionNotAllowed() {
        return new InboundValidationException(MessageUtils.getMessage("inbound.section.damaged.not.allowed"));
    }

    public static InboundValidationException capacityExceeded(int effectiveRemaining, int quantity) {
        return new InboundValidationException(
                MessageUtils.getMessage("inbound.section.capacity.exceeded", effectiveRemaining, quantity));
    }

    public static InboundValidationException noDefectRecorded() {
        return new InboundValidationException(MessageUtils.getMessage("inbound.defect.section.no.defect"));
    }

    public static InboundValidationException defectSectionTypeInvalid() {
        return new InboundValidationException(MessageUtils.getMessage("inbound.defect.section.type.invalid"));
    }

    public static InboundValidationException sectionUnassigned() {
        return new InboundValidationException(MessageUtils.getMessage("inbound.section.unassigned"));
    }

    public static InboundValidationException defectSectionUnassigned() {
        return new InboundValidationException(MessageUtils.getMessage("inbound.defect.section.unassigned"));
    }

    public static InboundValidationException sectionUnauthorized() {
        return new InboundValidationException(MessageUtils.getMessage("inbound.section.unauthorized"));
    }
}
