package com.example.sportswms.domain.order.api;

import com.example.sportswms.domain.order.api.dto.OrderResponseDTO;
import com.example.sportswms.domain.order.api.dto.AssignOrderRequestDTO;
import com.example.sportswms.domain.order.api.dto.OrderRequestDTO;
import com.example.sportswms.domain.order.api.dto.StoreAssignRequestDTO;
import com.example.sportswms.domain.order.api.dto.StoreRegisterRequestDTO;
import com.example.sportswms.domain.order.entity.StockOrder;
import com.example.sportswms.domain.order.service.StoreService;
import com.example.sportswms.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderApiController {

    private final StoreService storeService;

    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> getAllOrderDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(Map.of(
                "content", storeService.getAllOrderDetails(page, size).stream()
                        .map(OrderResponseDTO.StockOrderDetailDTO::from).toList(),
                "totalElements", storeService.countAllOrderDetails(),
                "totalPages", (int) Math.ceil((double) storeService.countAllOrderDetails() / size)
        ));
    }

    @GetMapping("/warehouse")
    public ResponseEntity<List<OrderResponseDTO.StockOrderDTO>> getMyWarehouseOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                storeService.findMyWarehouseOrders(userDetails.getUser()).stream()
                        .map(OrderResponseDTO.StockOrderDTO::from)
                        .toList());
    }

    @GetMapping("/warehouse/{stockOrderId}/details")
    public ResponseEntity<List<OrderResponseDTO.StockOrderDetailDTO>> getWarehouseOrderDetails(
            @PathVariable Long stockOrderId) {
        return ResponseEntity.ok(
                storeService.findOrderDetailsByStockOrderId(stockOrderId).stream()
                        .map(OrderResponseDTO.StockOrderDetailDTO::from)
                        .toList());
    }

    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyOrderDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(Map.of(
                "content", storeService.getOrderDetailsForAssignedStores(userId, page, size).stream()
                        .map(OrderResponseDTO.StockOrderDetailDTO::from).toList(),
                "totalElements", storeService.countOrderDetailsForAssignedStores(userId),
                "totalPages", (int) Math.ceil((double) storeService.countOrderDetailsForAssignedStores(userId) / size)
        ));
    }

    @PostMapping
    public ResponseEntity<List<OrderResponseDTO.StockOrderDetailDTO>> submitOrder(
            @Valid @RequestBody OrderRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                storeService.createStoreOrderRequest(dto.storeId(), dto.items(), userDetails.getUser()).stream()
                        .map(OrderResponseDTO.StockOrderDetailDTO::from)
                        .toList());
    }

    @PostMapping("/assign")
    public ResponseEntity<List<OrderResponseDTO.StockOrderDetailDTO>> assignOrderToWarehouse(
            @Valid @RequestBody AssignOrderRequestDTO dto) {
        StockOrder stockOrder = storeService.assignOrdersToWarehouse(dto);
        return ResponseEntity.ok(
                storeService.findOrderDetailsByStockOrderId(stockOrder.getId()).stream()
                        .map(OrderResponseDTO.StockOrderDetailDTO::from)
                        .toList());
    }

    @DeleteMapping("/{orderGroupId}")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable String orderGroupId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        storeService.cancelOrder(orderGroupId, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stores/management-types")
    public ResponseEntity<List<Map<String, String>>> getStoreManagementTypes() {
        return ResponseEntity.ok(
                Arrays.stream(com.example.sportswms.domain.order.entity.StoreManagementType.values())
                        .map(t -> Map.of("name", t.name(), "title", t.getTitle()))
                        .toList());
    }

    @GetMapping("/stores/my")
    public ResponseEntity<List<OrderResponseDTO.StoreDTO>> getMyStores(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                storeService.getMyStores(userDetails.getUser()).stream()
                        .map(OrderResponseDTO.StoreDTO::from)
                        .toList());
    }

    @GetMapping("/stores")
    public ResponseEntity<List<OrderResponseDTO.StoreDTO>> getAllStores() {
        return ResponseEntity.ok(
                storeService.getAllStores().stream()
                        .map(OrderResponseDTO.StoreDTO::from)
                        .toList());
    }

    @GetMapping("/stores/managers")
    public ResponseEntity<List<OrderResponseDTO.StoreManagerDTO>> getAllStoreManagers() {
        return ResponseEntity.ok(
                storeService.getAllStoreManagements().stream()
                        .map(OrderResponseDTO.StoreManagerDTO::from)
                        .toList());
    }

    @PostMapping("/stores")
    public ResponseEntity<OrderResponseDTO.StoreDTO> registerStore(
            @Valid @RequestBody StoreRegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OrderResponseDTO.StoreDTO.from(storeService.registerStore(dto)));
    }

    @PostMapping("/stores/assign")
    public ResponseEntity<OrderResponseDTO.StoreManagerDTO> assignStoreToUser(
            @Valid @RequestBody StoreAssignRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OrderResponseDTO.StoreManagerDTO.from(storeService.assignStoreToUser(dto)));
    }
}
