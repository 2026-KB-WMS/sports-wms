package com.example.sportswms.domain.outbound.api.dto;

import com.example.sportswms.domain.outbound.entity.Outbound;
import com.example.sportswms.domain.outbound.entity.OutboundDetail;

import java.time.LocalDateTime;

public class OutboundResponseDTO {

    public record OutboundDTO(
            Long id,
            Long warehouseId,
            String warehouseName,
            Long storeId,
            String storeName,
            Long stockOrderId,
            String status,
            String statusDescription,
            LocalDateTime completeTime
    ) {
        public static OutboundDTO from(Outbound o) {
            return new OutboundDTO(
                    o.getId(),
                    o.getWarehouse().getId(),
                    o.getWarehouse().getName(),
                    o.getStore().getId(),
                    o.getStore().getName(),
                    o.getStockOrder().getId(),
                    o.getStatus().name(),
                    o.getStatus().getDescription(),
                    o.getCompleteTime()
            );
        }
    }

    public record OutboundDetailDTO(
            Long id,
            Long skuId,
            String skuName,
            int quantity,
            Long sectionId,
            String sectionName
    ) {
        public static OutboundDetailDTO from(OutboundDetail d) {
            return new OutboundDetailDTO(
                    d.getId(),
                    d.getProductSKU().getId(),
                    d.getProductSKU().getName(),
                    d.getQuantity(),
                    d.getSection() != null ? d.getSection().getId() : null,
                    d.getSection() != null ? d.getSection().getName() : null
            );
        }
    }
}
