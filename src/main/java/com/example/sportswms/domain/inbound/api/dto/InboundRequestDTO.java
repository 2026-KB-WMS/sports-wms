package com.example.sportswms.domain.inbound.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record InboundRequestDTO(
        @NotNull(message = "{warehouse.selected}")
        Long warehouseId,

        @Valid
        List<InboundItemRequestDTO> items
) {
}
