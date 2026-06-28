package com.example.sportswms.domain.warehouse.api;

import com.example.sportswms.domain.inventory.service.InventoryService;
import com.example.sportswms.domain.warehouse.dto.SectionResponseDTO;
import com.example.sportswms.domain.warehouse.entity.SectionType;
import com.example.sportswms.domain.warehouse.entity.WarehouseManagementType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseApiController {

    private final InventoryService inventoryService;

    @GetMapping("/{warehouseId}/sections")
    public ResponseEntity<List<SectionResponseDTO>> getSectionsByWarehouse(@PathVariable Long warehouseId) {
        List<SectionResponseDTO> response = inventoryService.getSectionsByWarehouseId(warehouseId).stream()
                .map(SectionResponseDTO::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/section-types")
    public ResponseEntity<List<Map<String, String>>> getSectionTypes() {
        List<Map<String, String>> types = Arrays.stream(SectionType.values())
                .map(t -> Map.of("name", t.name(), "title", t.getTitle()))
                .toList();
        return ResponseEntity.ok(types);
    }

    @GetMapping("/management-types")
    public ResponseEntity<List<Map<String, String>>> getManagementTypes() {
        List<Map<String, String>> types = Arrays.stream(WarehouseManagementType.values())
                .map(t -> Map.of("name", t.name(), "roleName", t.getRoleName()))
                .toList();
        return ResponseEntity.ok(types);
    }
}
