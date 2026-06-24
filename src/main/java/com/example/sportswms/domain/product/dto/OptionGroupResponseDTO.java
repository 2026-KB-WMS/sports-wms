package com.example.sportswms.domain.product.dto;

import com.example.sportswms.domain.product.entity.OptionGroup;

import java.util.List;

public record OptionGroupResponseDTO(
        Long id,
        String name,
        List<OptionValueResponseDTO> optionValues
) {
    public static OptionGroupResponseDTO from(OptionGroup optionGroup) {
        return new OptionGroupResponseDTO(
                optionGroup.getId(),
                optionGroup.getName(),
                optionGroup.getOptionValues().stream()
                        .map(OptionValueResponseDTO::from)
                        .toList()
        );
    }
}
