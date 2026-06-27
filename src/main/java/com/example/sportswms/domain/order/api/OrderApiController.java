package com.example.sportswms.domain.order.api;

import com.example.sportswms.domain.order.api.dto.OrderResponse;
import com.example.sportswms.domain.order.dto.AssignOrderRequestDTO;
import com.example.sportswms.domain.order.dto.OrderRequestDTO;
import com.example.sportswms.domain.order.dto.StoreAssignRequestDTO;
import com.example.sportswms.domain.order.dto.StoreRegisterRequestDTO;
import com.example.sportswms.domain.order.entity.StockOrder;
import com.example.sportswms.domain.order.service.StoreService;
import com.example.sportswms.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderApiController {

    private final StoreService storeService;

    @GetMapping("/details")
    public ResponseEntity<List<OrderResponse.StockOrderDetailDTO>> getAllOrderDetails() {
        return ResponseEntity.ok(
                storeService.getAllOrderDetails().stream()
                        .map(OrderResponse.StockOrderDetailDTO::from)
                        .toList());
    }

    @GetMapping("/warehouse")
    public ResponseEntity<List<OrderResponse.StockOrderDTO>> getMyWarehouseOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                storeService.findMyWarehouseOrders(userDetails.getUser()).stream()
                        .map(OrderResponse.StockOrderDTO::from)
                        .toList());
    }

    @GetMapping("/warehouse/{stockOrderId}/details")
    public ResponseEntity<List<OrderResponse.StockOrderDetailDTO>> getWarehouseOrderDetails(
            @PathVariable Long stockOrderId) {
        return ResponseEntity.ok(
                storeService.findOrderDetailsByStockOrderId(stockOrderId).stream()
                        .map(OrderResponse.StockOrderDetailDTO::from)
                        .toList());
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse.StockOrderDetailDTO>> getMyOrderDetails(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                storeService.getOrderDetailsForAssignedStores(userDetails.getUser().getId()).stream()
                        .map(OrderResponse.StockOrderDetailDTO::from)
                        .toList());
    }

    @PostMapping
    public ResponseEntity<List<OrderResponse.StockOrderDetailDTO>> submitOrder(
            @Valid @RequestBody OrderRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                storeService.createStoreOrderRequest(dto.storeId(), dto.items()).stream()
                        .map(OrderResponse.StockOrderDetailDTO::from)
                        .toList());
    }

    @PostMapping("/assign")
    public ResponseEntity<List<OrderResponse.StockOrderDetailDTO>> assignOrderToWarehouse(
            @Valid @RequestBody AssignOrderRequestDTO dto) {
        StockOrder stockOrder = storeService.assignOrdersToWarehouse(dto);
        return ResponseEntity.ok(
                storeService.findOrderDetailsByStockOrderId(stockOrder.getId()).stream()
                        .map(OrderResponse.StockOrderDetailDTO::from)
                        .toList());
    }

    @DeleteMapping("/{orderGroupId}")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable String orderGroupId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        storeService.cancelOrder(orderGroupId, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stores")
    public ResponseEntity<List<OrderResponse.StoreDTO>> getAllStores() {
        return ResponseEntity.ok(
                storeService.getAllStores().stream()
                        .map(OrderResponse.StoreDTO::from)
                        .toList());
    }

    @GetMapping("/stores/managers")
    public ResponseEntity<List<OrderResponse.StoreManagerDTO>> getAllStoreManagers() {
        return ResponseEntity.ok(
                storeService.getAllStoreManagements().stream()
                        .map(OrderResponse.StoreManagerDTO::from)
                        .toList());
    }

    @PostMapping("/stores")
    public ResponseEntity<OrderResponse.StoreDTO> registerStore(
            @Valid @RequestBody StoreRegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OrderResponse.StoreDTO.from(storeService.registerStore(dto)));
    }

    @PostMapping("/stores/assign")
    public ResponseEntity<OrderResponse.StoreManagerDTO> assignStoreToUser(
            @Valid @RequestBody StoreAssignRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OrderResponse.StoreManagerDTO.from(storeService.assignStoreToUser(dto)));
    }
}
