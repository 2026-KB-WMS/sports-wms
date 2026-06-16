package com.example.sportswms.domain.order.dto;

import jakarta.validation.constraints.NotNull;

public record OrderItemRequestDTO(
    @NotNull(message = "{sku.product.selected}")
    Long skuId,

    @NotNull(message = "{order.quantity.required}")
    int quantity,

    String memo
) {}