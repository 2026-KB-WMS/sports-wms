package com.example.sportswms.domain.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequestDTO(
    @NotNull(message = "{sku.product.selected}")
    Long skuId,

    @Min(value = 1, message = "{quantity.minimum}")
    int quantity,

    String memo
) {}