package com.example.sportswms.domain.order.service;

import com.example.sportswms.domain.order.dto.AssignOrderRequestDTO;
import com.example.sportswms.domain.order.dto.OrderItemRequestDTO;
import com.example.sportswms.domain.order.dto.StoreAssignRequestDTO;
import com.example.sportswms.domain.order.dto.StoreRegisterRequestDTO;
import com.example.sportswms.domain.order.entity.StockOrder;
import com.example.sportswms.domain.order.entity.StockOrderDetail;import com.example.sportswms.domain.order.entity.Store;
import com.example.sportswms.domain.order.entity.StoreManagement;
import com.example.sportswms.domain.order.entity.OrderDetailStatus;
import com.example.sportswms.domain.order.repository.StockOrderDetailRepository;
import com.example.sportswms.domain.order.repository.StockOrderRepository;
import com.example.sportswms.domain.order.repository.StoreManagementRepository;
import com.example.sportswms.domain.order.repository.StoreRepository;
import com.example.sportswms.domain.outbound.service.OutboundService;
import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.product.repository.ProductSKURepository;
import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.user.repository.UserRepository;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import com.example.sportswms.domain.warehouse.entity.WarehouseManagement;
import com.example.sportswms.domain.warehouse.repository.WarehouseManagementRepository;
import com.example.sportswms.domain.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.sportswms.global.util.MessageUtils.getMessage;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreManagementRepository storeManagementRepository;
    private final StockOrderRepository stockOrderRepository;
    private final StockOrderDetailRepository stockOrderDetailRepository;
    private final ProductSKURepository productSKURepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseManagementRepository warehouseManagementRepository;
    private final OutboundService outboundService;

    public List<StockOrder> findMyWarehouseOrders(User user) {

        List<WarehouseManagement> warehouseManagements = warehouseManagementRepository.findAllByUser(user);
        List<Warehouse> warehouses = warehouseManagements.stream()
                .map(WarehouseManagement::getWarehouse)
                .collect(Collectors.toList());

        if (warehouses.isEmpty()) {
            return List.of();
        }

        return stockOrderRepository.findAllByWarehouseIn(warehouses);
    }

    public List<StockOrderDetail> findOrderDetailsByStockOrderId(Long stockOrderId) {
        StockOrder stockOrder = stockOrderRepository.findById(stockOrderId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("order.invalid")));
        return stockOrderDetailRepository.findAllByStockOrder(stockOrder);
    }

    public List<Store> getAllStores() { return storeRepository.findAll(); }
    public List<StoreManagement> getAllStoreManagements() { return storeManagementRepository.findAll(); }    public List<Store> getAssignedStoresByUserId(Long userId) {
        return storeManagementRepository.findByUserId(userId).stream()
                .map(StoreManagement::getStore)
                .toList();
    }

    public List<StockOrderDetail> getOrderDetailsForAssignedStores(Long userId) {
        List<Store> assignedStores = getAssignedStoresByUserId(userId);
        if (assignedStores.isEmpty()) {
            return List.of();
        }
        return stockOrderDetailRepository.findByStoreIn(assignedStores);
    }
    
    public List<StockOrderDetail> getAllOrderDetails() {
        return stockOrderDetailRepository.findAll();
    }

    @Transactional
    public StockOrder assignOrdersToWarehouse(AssignOrderRequestDTO dto) {
        Warehouse warehouse = warehouseRepository.findById(dto.warehouseId())
                .orElseThrow(() -> new IllegalArgumentException(getMessage("warehouse.invalid")));

        List<StockOrderDetail> detailsToAssign = stockOrderDetailRepository.findAllById(dto.orderDetailIds());

        if (detailsToAssign.isEmpty()) {
            throw new IllegalArgumentException(getMessage("order.detail.selected"));
        }

        // 이미 다른 StockOrder에 할당된 요청인지 확인
        boolean alreadyAssigned = detailsToAssign.stream().anyMatch(detail -> detail.getStockOrder() != null);
        if (alreadyAssigned) {
            throw new IllegalStateException(getMessage("order.detail.assigned"));
        }

        StockOrder stockOrder = StockOrder.create(warehouse);
        stockOrderRepository.save(stockOrder);

        for (StockOrderDetail detail : detailsToAssign) {
            detail.assignStockOrder(stockOrder);
        }

        // 발주가 창고에 위임되는 시점에 대응하는 출고 요청을 함께 생성
        outboundService.createOutboundFromStockOrder(warehouse, stockOrder, detailsToAssign);
        return stockOrder;
    }

    @Transactional
    public List<StockOrderDetail> createStoreOrderRequest(Long storeId, List<OrderItemRequestDTO> items) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("store.invalid")));

        String uniqueGroupId = "REQ-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        List<StockOrderDetail> details = items.stream().map(itemDto -> {
            ProductSKU sku = productSKURepository.findById(itemDto.skuId())
                    .orElseThrow(() -> new IllegalArgumentException(getMessage("sku.invalid")));
            return StockOrderDetail.from(store, uniqueGroupId, sku, itemDto);
        }).toList();

        return stockOrderDetailRepository.saveAll(details);
    }

    @Transactional
    public Store registerStore(StoreRegisterRequestDTO dto) {
        return storeRepository.save(Store.from(dto));
    }

    @Transactional
    public void cancelOrder(String orderGroupId, User requestUser) {
        List<StockOrderDetail> details = stockOrderDetailRepository.findAllByOrderGroupId(orderGroupId);

        if (details.isEmpty()) {
            throw new IllegalArgumentException(getMessage("order.invalid"));
        }

        // 본인 발주인지 확인
        Store store = details.get(0).getStore();
        boolean isOwner = storeManagementRepository.findByUserId(requestUser.getId()).stream()
                .anyMatch(sm -> sm.getStore().getId().equals(store.getId()));
        if (!isOwner) {
            throw new IllegalStateException(getMessage("order.cancel.unauthorized"));
        }

        // PENDING 상태인지 확인 (하나라도 PENDING이 아니면 취소 불가)
        boolean hasNonPending = details.stream()
                .anyMatch(d -> d.getStatus() != OrderDetailStatus.PENDING);
        if (hasNonPending) {
            throw new IllegalStateException(getMessage("order.cancel.not.allowed"));
        }

        details.forEach(StockOrderDetail::cancel);
    }

    @Transactional
    public StoreManagement assignStoreToUser(StoreAssignRequestDTO dto) {
        Store store = storeRepository.findById(dto.storeId())
                .orElseThrow(() -> new IllegalArgumentException(getMessage("store.invalid")));
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException(getMessage("user.invalid")));
        if (storeManagementRepository.existsByStoreAndUser(store, user)) {
            throw new IllegalStateException(getMessage("store.user.assigned"));
        }
        StoreManagement storeManagement = StoreManagement.of(store, user, dto.storeManagementType());
        return storeManagementRepository.save(storeManagement);
    }

}