package com.example.sportswms.global.exception.warehouse;

import com.example.sportswms.global.util.MessageUtils;
import org.springframework.http.HttpStatus;

/** 구역명 중복, 담당자 중복 배정, 사용 중인 구역 삭제 시도 등 → 409 Conflict */
public class WarehouseConflictException extends WarehouseException {
    private WarehouseConflictException(String message) {
        super(message);
    }

    public static WarehouseConflictException sectionNameDuplicate() {
        return new WarehouseConflictException(MessageUtils.getMessage("section.name.duplicate"));
    }

    public static WarehouseConflictException managerAlreadyAssigned() {
        return new WarehouseConflictException(MessageUtils.getMessage("management.assignment.duplicate"));
    }

    public static WarehouseConflictException sectionInUse() {
        return new WarehouseConflictException(MessageUtils.getMessage("section.delete.in.use"));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}
