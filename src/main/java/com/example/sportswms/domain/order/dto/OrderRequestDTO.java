package com.example.sportswms.domain.order.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequestDTO(
        @NotNull(message = "지점을 선택해주세요.")
        Long storeId,
        List<OrderItemRequestDTO> items
) {}