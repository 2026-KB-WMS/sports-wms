package com.example.sportswms.domain.inventory.api;

import com.example.sportswms.domain.inventory.api.dto.InventoryResponseDTO;
import com.example.sportswms.domain.inventory.service.InventoryService;
import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryApiController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<Page<InventoryResponseDTO.InventoryDTO>> getInventories(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long skuId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(
                inventoryService.getInventories(warehouseId, sectionId, skuId, user, page, size)
                        .map(InventoryResponseDTO.InventoryDTO::from));
    }

    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Object>> getTransactions(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long skuId,
            @RequestParam(required = false) String cursorCreatedAt,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "15") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();

        LocalDateTime parsedCursor = cursorCreatedAt != null
                ? LocalDateTime.parse(cursorCreatedAt)
                : null;

        InventoryService.CursorResult<com.example.sportswms.domain.inventory.entity.InventoryTransaction> result =
                inventoryService.getTransactions(warehouseId, sectionId, skuId, user, parsedCursor, cursorId, size);

        List<InventoryResponseDTO.TransactionDTO> content = result.content().stream()
                .map(InventoryResponseDTO.TransactionDTO::from)
                .toList();

        // 다음 페이지 커서: 현재 페이지 마지막 레코드의 createdAt, id
        Map<String, Object> response;
        if (!content.isEmpty() && result.hasNext()) {
            InventoryResponseDTO.TransactionDTO last = content.get(content.size() - 1);
            response = Map.of(
                    "content", content,
                    "hasNext", true,
                    "nextCursorCreatedAt", last.createdAt().toString(),
                    "nextCursorId", last.id()
            );
        } else {
            response = Map.of(
                    "content", content,
                    "hasNext", false
            );
        }
        return ResponseEntity.ok(response);
    }
}
