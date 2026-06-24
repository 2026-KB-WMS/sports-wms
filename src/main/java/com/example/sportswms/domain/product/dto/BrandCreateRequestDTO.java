package com.example.sportswms.domain.product.dto;

import jakarta.validation.constraints.NotBlank;

public record BrandCreateRequestDTO(
        @NotBlank(message = "{brand.name.required}") String name,
        @NotBlank(message = "{brand.code.required}") String code
) {}
