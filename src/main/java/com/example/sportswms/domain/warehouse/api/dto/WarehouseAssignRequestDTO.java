package com.example.sportswms.domain.warehouse.api.dto;

import com.example.sportswms.domain.warehouse.entity.WarehouseManagementType;
import jakarta.validation.constraints.NotNull;

public record WarehouseAssignRequestDTO(
        @NotNull(message = "{management.warehouse.selected}")
        Long warehouseId,

        @NotNull(message = "{management.user.selected}")
        Long userId,

        @NotNull(message = "{management.type.selected}")
        WarehouseManagementType managementType
) {
}
