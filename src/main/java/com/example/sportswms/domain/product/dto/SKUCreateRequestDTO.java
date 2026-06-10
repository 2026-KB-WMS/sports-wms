package com.example.sportswms.domain.product.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SKUCreateRequestDTO(
        @NotNull(message = "{sku.product.selected}") Long productId,
        @NotEmpty(message = "{sku.option.selected}") List<Long> optionValueIds
) {}