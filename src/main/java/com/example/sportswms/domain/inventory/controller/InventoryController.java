package com.example.sportswms.domain.inventory.controller;

import com.example.sportswms.domain.inventory.service.InventoryService;
import com.example.sportswms.domain.warehouse.dto.SectionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/inventory")
    public String inventoryPage(@RequestParam(required = false) Long warehouseId,
                                @RequestParam(required = false) Long sectionId,
                                @RequestParam(required = false) Long skuId,
                                Model model) {
        model.addAttribute("inventories",  inventoryService.getInventories(warehouseId, sectionId, skuId));
        model.addAttribute("transactions", inventoryService.getTransactions(warehouseId, sectionId, skuId));
        model.addAttribute("warehouses",   inventoryService.getAllWarehouses());
        model.addAttribute("sections",     inventoryService.getAllSections());
        model.addAttribute("skus",         inventoryService.getAllSKUs());
        return "inventory";
    }

    @ResponseBody
    @GetMapping("/api/warehouses/{warehouseId}/sections")
    public ResponseEntity<List<SectionResponseDTO>> getSectionsByWarehouse(@PathVariable Long warehouseId) {
        List<SectionResponseDTO> response = inventoryService.getSectionsByWarehouseId(warehouseId).stream()
                .map(SectionResponseDTO::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}
