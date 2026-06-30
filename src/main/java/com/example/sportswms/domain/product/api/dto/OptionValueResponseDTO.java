package com.example.sportswms.domain.product.api.dto;

import com.example.sportswms.domain.product.entity.OptionValue;

public record OptionValueResponseDTO(
        Long id,
        String name
) {
    public static OptionValueResponseDTO from(OptionValue optionValue) {
        return new OptionValueResponseDTO(optionValue.getId(), optionValue.getName());
    }
}
