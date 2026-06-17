package com.example.sportswms.domain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequestDTO(
        @NotNull(message = "{store.selected}")
        Long storeId,

        @Valid
        List<OrderItemRequestDTO> items
) {}