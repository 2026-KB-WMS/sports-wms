package com.example.sportswms.domain.order.dto;

import jakarta.validation.constraints.NotNull;

public record OrderRequestDTO(
    @NotNull(message = "상품을 선택해주세요.")
    Long skuId,

    @NotNull(message = "수량을 입력해주세요.")
    int quantity
) {}