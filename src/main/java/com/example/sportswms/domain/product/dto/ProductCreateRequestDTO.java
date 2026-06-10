package com.example.sportswms.domain.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProductCreateRequestDTO(
        @NotBlank(message = "{product.name.required}") String name,
        @NotBlank(message = "{product.code.required}") String code,
        @Min(value = 0, message = "{product.price.minimum}") int price
) {}