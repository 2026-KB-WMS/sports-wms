package com.example.sportswms.domain.warehouse.api;

import com.example.sportswms.domain.inventory.service.InventoryService;
import com.example.sportswms.domain.warehouse.dto.SectionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
