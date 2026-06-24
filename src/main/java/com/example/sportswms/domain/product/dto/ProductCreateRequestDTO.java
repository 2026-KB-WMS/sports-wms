package com.example.sportswms.domain.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductCreateRequestDTO(
        @NotBlank(message = "{product.name.required}") String name,
        @NotBlank(message = "{product.code.required}") String code,
        @Min(value = 1000, message = "{product.price.minimum}") int price,
        @NotNull(message = "{product.brand.required}") Long brandId,
        @NotNull(message = "{product.category.required}") Long categoryId,
        List<Long> specOptionValueIds   // SPEC 타입 옵션값 ID 목록 (없을 수도 있음)
) {}
