package com.example.sportswms.domain.order.dto;

import com.example.sportswms.domain.order.entity.StoreManagementType;
import jakarta.validation.constraints.NotNull;

public record StoreAssignRequestDTO(
        @NotNull(message = "할당할 지점 ID는 필수 항목입니다.")
        Long storeId,

        @NotNull(message = "지점을 배정받을 회원 ID는 필수 항목입니다.")
        Long userId,

        @NotNull(message = "지점 관리 권한 타입은 필수 항목입니다.")
        StoreManagementType storeManagementType
) {
}