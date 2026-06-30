package com.example.sportswms.domain.inbound.api.dto;

import com.example.sportswms.domain.inbound.entity.InboundDetail;
import java.util.List;

public record InboundDetailViewDTO(
        Long id, String skuName, int quantity, int defectQuantity, int normalQuantity,
        String sectionName, String defectSectionName, String supplierName,
        boolean assignable,
        boolean clearable,
        boolean defectAssignable,
        boolean defectClearable,
        boolean defectRecorded,
        List<SectionOptionDTO> sections,
        List<SectionOptionDTO> defectSections
) {
    public static InboundDetailViewDTO of(InboundDetail detail, boolean isInspecting,
                                          List<SectionOptionDTO> sections,
                                          List<SectionOptionDTO> defectSections) {
        boolean assigned       = detail.getSection() != null;
        boolean defectAssigned = detail.getDefectSection() != null;
        boolean hasDefect      = detail.getDefectQuantity() > 0;

        return new InboundDetailViewDTO(
                detail.getId(),
                detail.getProductSKU().getName(),
                detail.getQuantity(),
                detail.getDefectQuantity(),
                detail.getNormalQuantity(),
                assigned       ? detail.getSection().getName()       : null,
                defectAssigned ? detail.getDefectSection().getName() : null,
                detail.getSupplier() != null ? detail.getSupplier().getName() : null,
                isInspecting && !assigned,
                isInspecting && assigned,
                isInspecting && hasDefect && !defectAssigned,
                isInspecting && hasDefect && defectAssigned,
                detail.isDefectRecorded(),
                isInspecting && !assigned                             ? sections      : List.of(),
                isInspecting && hasDefect && !defectAssigned          ? defectSections : List.of()
        );
    }

    public record SectionOptionDTO(Long id, String name, String sectionCode, int effectiveRemaining) {}
}
