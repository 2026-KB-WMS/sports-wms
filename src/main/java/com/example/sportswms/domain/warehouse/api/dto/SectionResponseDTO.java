package com.example.sportswms.domain.warehouse.api.dto;

import com.example.sportswms.domain.warehouse.entity.Section;

public record SectionResponseDTO(
        Long id,
        String name
) {
    public static SectionResponseDTO from(Section section) {
        return new SectionResponseDTO(section.getId(), section.getName());
    }
}
