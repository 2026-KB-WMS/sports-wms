package com.example.sportswms.domain.warehouse.api;

import com.example.sportswms.domain.warehouse.api.dto.WarehouseResponse;
import com.example.sportswms.domain.warehouse.dto.SectionCreateRequestDTO;
import com.example.sportswms.domain.warehouse.dto.WarehouseAssignRequestDTO;
import com.example.sportswms.domain.warehouse.dto.WarehouseCreateRequestDTO;
import com.example.sportswms.domain.warehouse.service.WarehouseService;
import com.example.sportswms.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseManagementApiController {

    private final WarehouseService warehouseService;

    @GetMapping("/my")
    public ResponseEntity<List<WarehouseResponse.WarehouseDTO>> getMyWarehouses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                warehouseService.findMyWarehouses(userDetails.getUser()).stream()
                        .map(WarehouseResponse.WarehouseDTO::from)
                        .toList());
    }

    @GetMapping
    public ResponseEntity<List<WarehouseResponse.WarehouseDTO>> getAllWarehouses() {
        return ResponseEntity.ok(
                warehouseService.getAllWarehouses().stream()
                        .map(WarehouseResponse.WarehouseDTO::from)
                        .toList());
    }

    @GetMapping("/sections")
    public ResponseEntity<List<WarehouseResponse.SectionDTO>> getAllSections() {
        return ResponseEntity.ok(
                warehouseService.getAllSections().stream()
                        .map(WarehouseResponse.SectionDTO::from)
                        .toList());
    }

    @PostMapping
    public ResponseEntity<WarehouseResponse.WarehouseDTO> createWarehouse(
            @Valid @RequestBody WarehouseCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(WarehouseResponse.WarehouseDTO.from(warehouseService.createWarehouse(dto)));
    }

    @PostMapping("/sections")
    public ResponseEntity<WarehouseResponse.SectionDTO> createSection(
            @Valid @RequestBody SectionCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(WarehouseResponse.SectionDTO.from(warehouseService.createSection(dto)));
    }

    @DeleteMapping("/sections/{sectionId}")
    public ResponseEntity<Void> deleteSection(@PathVariable Long sectionId) {
        warehouseService.deleteSection(sectionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/managers")
    public ResponseEntity<List<WarehouseResponse.WarehouseManagerDTO>> getAllManagers() {
        return ResponseEntity.ok(
                warehouseService.getAllWarehouseManagements().stream()
                        .map(WarehouseResponse.WarehouseManagerDTO::from)
                        .toList());
    }

    @PostMapping("/managers")
    public ResponseEntity<WarehouseResponse.WarehouseManagerDTO> assignManager(
            @Valid @RequestBody WarehouseAssignRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(WarehouseResponse.WarehouseManagerDTO.from(warehouseService.assignWarehouseManager(dto)));
    }
}
