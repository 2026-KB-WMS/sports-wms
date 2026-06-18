package com.example.sportswms.domain.inbound.dto;

import com.example.sportswms.domain.inbound.entity.InboundDetail;
import com.example.sportswms.domain.warehouse.entity.Section;
import java.util.List;

public record InboundDetailViewDTO(
        Long id, String skuName, int quantity,
        String sectionName, String supplierName,
        boolean assignable, List<Section> sections
) {
    public static InboundDetailViewDTO of(InboundDetail detail, boolean isInspecting, List<Section> sections) {
        boolean assigned = detail.getSection() != null;
        boolean assignable = isInspecting && !assigned;
        return new InboundDetailViewDTO(
                detail.getId(), detail.getProductSKU().getName(), detail.getQuantity(),
                assigned ? detail.getSection().getName() : null,
                detail.getSupplier() != null ? detail.getSupplier().getName() : null,
                assignable, assignable ? sections : List.of()
        );
    }
}