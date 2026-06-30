package com.example.sportswms.domain.inventory.api.dto;

import com.example.sportswms.domain.inventory.entity.Inventory;
import com.example.sportswms.domain.inventory.entity.InventoryTransaction;

import java.time.LocalDateTime;

public class InventoryResponseDTO {

    public record InventoryDTO(
            Long id,
            Long sectionId,
            String sectionName,
            String sectionCode,
            Long warehouseId,
            String warehouseName,
            Long skuId,
            String skuName,
            int actualQuantity,
            int allocatedQuantity,
            int availableQuantity,
            String status
    ) {
        public static InventoryDTO from(Inventory i) {
            return new InventoryDTO(
                    i.getId(),
                    i.getSection().getId(),
                    i.getSection().getName(),
                    i.getSection().getSectionCode(),
                    i.getSection().getWarehouse().getId(),
                    i.getSection().getWarehouse().getName(),
                    i.getProductSKU().getId(),
                    i.getProductSKU().getName(),
                    i.getActualQuantity(),
                    i.getAllocatedQuantity(),
                    i.getAvailableQuantity(),
                    i.getStatus().name()
            );
        }
    }

    public record TransactionDTO(
            Long id,
            Long sectionId,
            String sectionName,
            Long skuId,
            String skuName,
            String transactionType,
            String transactionTypeTitle,
            int quantity,
            int beforeQuantity,
            int afterQuantity,
            String reason,
            LocalDateTime createdAt
    ) {
        public static TransactionDTO from(InventoryTransaction t) {
            return new TransactionDTO(
                    t.getId(),
                    t.getSection().getId(),
                    t.getSection().getName(),
                    t.getProductSKU().getId(),
                    t.getProductSKU().getName(),
                    t.getTransactionType().name(),
                    t.getTransactionType().getTitle(),
                    t.getQuantity(),
                    t.getBeforeQuantity(),
                    t.getAfterQuantity(),
                    t.getReason(),
                    t.getCreatedAt()
            );
        }
    }
}
