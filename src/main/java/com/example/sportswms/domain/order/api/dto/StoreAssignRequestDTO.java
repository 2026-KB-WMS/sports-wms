package com.example.sportswms.domain.order.api.dto;

import com.example.sportswms.domain.order.entity.StoreManagementType;
import jakarta.validation.constraints.NotNull;

public record StoreAssignRequestDTO(
        @NotNull(message = "{store.selected}")
        Long storeId,

        @NotNull(message = "{user.selected}")
        Long userId,

        @NotNull(message = "{store.managementType.selected}")
        StoreManagementType storeManagementType
) {
}
