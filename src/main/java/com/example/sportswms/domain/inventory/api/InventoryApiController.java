package com.example.sportswms.domain.inventory.api;

import com.example.sportswms.domain.inventory.api.dto.InventoryResponseDTO;
import com.example.sportswms.domain.inventory.service.InventoryService;
import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<List<InventoryResponseDTO.InventoryDTO>> getInventories(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long skuId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(
                inventoryService.getInventories(warehouseId, sectionId, skuId, user).stream()
                        .map(InventoryResponseDTO.InventoryDTO::from)
                        .toList());
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<InventoryResponseDTO.TransactionDTO>> getTransactions(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long skuId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(
                inventoryService.getTransactions(warehouseId, sectionId, skuId, user).stream()
                        .map(InventoryResponseDTO.TransactionDTO::from)
                        .toList());
    }
}
