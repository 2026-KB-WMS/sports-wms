package com.example.sportswms.domain.order.api.dto;

import com.example.sportswms.domain.order.entity.StockOrder;
import com.example.sportswms.domain.order.entity.StockOrderDetail;
import com.example.sportswms.domain.order.entity.Store;
import com.example.sportswms.domain.order.entity.StoreManagement;

import java.time.LocalDateTime;

public class OrderResponseDTO {

    public record StoreDTO(Long id, String name, String address, String callNum) {
        public static StoreDTO from(Store s) {
            return new StoreDTO(s.getId(), s.getName(), s.getAddress(), s.getCallNum());
        }
    }

    public record StoreManagerDTO(
            Long id,
            Long storeId,
            String storeName,
            Long userId,
            String userName,
            String managementType,
            String managementTypeTitle
    ) {
        public static StoreManagerDTO from(StoreManagement sm) {
            return new StoreManagerDTO(
                    sm.getId(),
                    sm.getStore().getId(),
                    sm.getStore().getName(),
                    sm.getUser().getId(),
                    sm.getUser().getName(),
                    sm.getStoreManagementType().name(),
                    sm.getStoreManagementType().getTitle()
            );
        }
    }

    public record StockOrderDTO(
            Long id,
            Long warehouseId,
            String warehouseName,
            String status,
            String statusDescription,
            LocalDateTime requestTime,
            LocalDateTime completeTime
    ) {
        public static StockOrderDTO from(StockOrder o) {
            return new StockOrderDTO(
                    o.getId(),
                    o.getWarehouse().getId(),
                    o.getWarehouse().getName(),
                    o.getStatus().name(),
                    o.getStatus().getDescription(),
                    o.getRequestTime(),
                    o.getCompleteTime()
            );
        }
    }

    public record StockOrderDetailDTO(
            Long id,
            String orderGroupId,
            Long storeId,
            String storeName,
            Long skuId,
            String skuName,
            int quantity,
            String memo,
            String status,
            String statusDescription
    ) {
        public static StockOrderDetailDTO from(StockOrderDetail d) {
            return new StockOrderDetailDTO(
                    d.getId(),
                    d.getOrderGroupId(),
                    d.getStore().getId(),
                    d.getStore().getName(),
                    d.getProductSKU().getId(),
                    d.getProductSKU().getName(),
                    d.getQuantity(),
                    d.getMemo(),
                    d.getStatus().name(),
                    d.getStatus().getDescription()
            );
        }
    }
}
