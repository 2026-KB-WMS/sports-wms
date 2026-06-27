package com.example.sportswms.domain.warehouse.api.dto;

import com.example.sportswms.domain.warehouse.entity.Section;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import com.example.sportswms.domain.warehouse.entity.WarehouseManagement;

public class WarehouseResponse {

    public record WarehouseDTO(
            Long id,
            String name,
            String address,
            int totalCapacity,
            int currentSectionCapacity
    ) {
        public static WarehouseDTO from(Warehouse w) {
            return new WarehouseDTO(w.getId(), w.getName(), w.getAddress(),
                    w.getTotalCapacity(), w.getCurrentSectionCapacity());
        }
    }

    public record SectionDTO(
            Long id,
            String name,
            String sectionCode,
            String sectionType,
            String sectionTypeTitle,
            int totalCapacity,
            int currentUsage,
            int remainingCapacity,
            Long warehouseId,
            String warehouseName
    ) {
        public static SectionDTO from(Section s) {
            return new SectionDTO(
                    s.getId(), s.getName(), s.getSectionCode(),
                    s.getSectionType().name(),
                    s.getSectionType().getTitle(),
                    s.getTotalCapacity(), s.getCurrentUsage(), s.getRemainingCapacity(),
                    s.getWarehouse().getId(), s.getWarehouse().getName()
            );
        }
    }

    public record WarehouseManagerDTO(
            Long id,
            Long warehouseId,
            String warehouseName,
            Long userId,
            String userName,
            String managementType,
            String managementTypeTitle
    ) {
        public static WarehouseManagerDTO from(WarehouseManagement wm) {
            return new WarehouseManagerDTO(
                    wm.getId(),
                    wm.getWarehouse().getId(),
                    wm.getWarehouse().getName(),
                    wm.getUser().getId(),
                    wm.getUser().getName(),
                    wm.getManagementType().name(),
                    wm.getManagementType().getRoleName()
            );
        }
    }
}
