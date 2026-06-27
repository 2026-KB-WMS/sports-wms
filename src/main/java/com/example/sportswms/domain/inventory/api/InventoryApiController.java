package com.example.sportswms.domain.inventory.api;

import com.example.sportswms.domain.inventory.api.dto.InventoryResponse;
import com.example.sportswms.domain.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryApiController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryResponse.InventoryDTO>> getInventories(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long skuId) {
        List<InventoryResponse.InventoryDTO> data =
                inventoryService.getInventories(warehouseId, sectionId, skuId).stream()
                        .map(InventoryResponse.InventoryDTO::from)
                        .toList();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<InventoryResponse.TransactionDTO>> getTransactions(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long skuId) {
        List<InventoryResponse.TransactionDTO> data =
                inventoryService.getTransactions(warehouseId, sectionId, skuId).stream()
                        .map(InventoryResponse.TransactionDTO::from)
                        .toList();
        return ResponseEntity.ok(data);
    }
}
