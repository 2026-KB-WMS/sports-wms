package com.example.sportswms.domain.inbound.api.dto;

import com.example.sportswms.domain.inbound.entity.Inbound;
import com.example.sportswms.domain.inbound.entity.InboundDetail;

import java.time.LocalDateTime;

public class InboundResponse {

    public record InboundDTO(
            Long id,
            Long warehouseId,
            String warehouseName,
            String status,
            String statusDescription,
            LocalDateTime requestTime,
            LocalDateTime completeTime
    ) {
        public static InboundDTO from(Inbound inbound) {
            return new InboundDTO(
                    inbound.getId(),
                    inbound.getWarehouse().getId(),
                    inbound.getWarehouse().getName(),
                    inbound.getStatus().name(),
                    inbound.getStatus().getDescription(),
                    inbound.getRequestTime(),
                    inbound.getCompleteTime()
            );
        }
    }

    public record InboundDetailDTO(
            Long id,
            Long skuId,
            String skuName,
            int quantity,
            int defectQuantity,
            int normalQuantity,
            boolean defectRecorded,
            Long sectionId,
            String sectionName,
            Long defectSectionId,
            String defectSectionName
    ) {
        public static InboundDetailDTO from(InboundDetail d) {
            return new InboundDetailDTO(
                    d.getId(),
                    d.getProductSKU().getId(),
                    d.getProductSKU().getName(),
                    d.getQuantity(),
                    d.getDefectQuantity(),
                    d.getNormalQuantity(),
                    d.isDefectRecorded(),
                    d.getSection() != null ? d.getSection().getId() : null,
                    d.getSection() != null ? d.getSection().getName() : null,
                    d.getDefectSection() != null ? d.getDefectSection().getId() : null,
                    d.getDefectSection() != null ? d.getDefectSection().getName() : null
            );
        }
    }
}
