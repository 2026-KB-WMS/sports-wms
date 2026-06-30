package com.example.sportswms.domain.inbound.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InboundItemRequestDTO(
        @NotNull(message = "{sku.product.selected}")
        Long skuId,

        @Min(value = 1, message = "{quantity.minimum}")
        int quantity
) {
}
