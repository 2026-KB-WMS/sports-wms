package com.example.sportswms.domain.outbound.service;

import com.example.sportswms.domain.inventory.entity.Inventory;
import com.example.sportswms.domain.inventory.entity.InventoryStatus;
import com.example.sportswms.domain.inventory.entity.InventoryTransaction;
import com.example.sportswms.domain.inventory.entity.TransactionType;
import com.example.sportswms.domain.inventory.repository.InventoryRepository;
import com.example.sportswms.domain.inventory.repository.InventoryTransactionRepository;
import com.example.sportswms.domain.order.entity.StockOrder;
import com.example.sportswms.domain.order.entity.StockOrderDetail;
import com.example.sportswms.domain.outbound.entity.Outbound;
import com.example.sportswms.domain.outbound.entity.OutboundDetail;
import com.example.sportswms.domain.outbound.entity.OutboundStatus;
import com.example.sportswms.domain.outbound.repository.OutboundDetailRepository;
import com.example.sportswms.domain.outbound.repository.OutboundRepository;
import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.warehouse.entity.Section;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import com.example.sportswms.domain.warehouse.entity.WarehouseManagement;
import com.example.sportswms.domain.warehouse.repository.SectionRepository;
import com.example.sportswms.domain.warehouse.repository.WarehouseManagementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.sportswms.global.util.MessageUtils.getMessage;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OutboundService {
    private final OutboundRepository outboundRepository;
    private final OutboundDetailRepository outboundDetailRepository;
    private final SectionRepository sectionRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final WarehouseManagementRepository warehouseManagementRepository;

    public Outbound getOutbound(Long outboundId) {
        return outboundRepository.findById(outboundId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("outbound.invalid")));
    }

    public List<OutboundDetail> getOutboundDetails(Long outboundId, User user) {
        Outbound outbound = getOutbound(outboundId);
        validateWarehouseAccess(outbound.getWarehouse(), user);
        return outboundDetailRepository.findByOutboundId(outboundId);
    }

    /**
     * 본사관리자가 발주 상세를 창고에 위임(StockOrder)할 때, 동일 트랜잭션에서
     * 대응하는 출고 요청(Outbound/OutboundDetail)을 함께 생성한다.
     * 지점(Store) 단위로 Outbound를 분리한다.
     * 이 시점에는 재고를 할당하지 않는다 (창고관리자가 구역 배정 시 할당).
     */
    @Transactional
    public void createOutboundFromStockOrder(Warehouse warehouse, StockOrder stockOrder, List<StockOrderDetail> stockOrderDetails) {
        Map<Long, List<StockOrderDetail>> detailsByStoreId = stockOrderDetails.stream()
                .collect(Collectors.groupingBy(detail -> detail.getStore().getId()));

        detailsByStoreId.forEach((storeId, detailsForStore) -> {
            Outbound outbound = Outbound.create(warehouse, stockOrder);
            outboundRepository.save(outbound);

            List<OutboundDetail> outboundDetails = detailsForStore.stream()
                    .map(detail -> OutboundDetail.from(outbound, detail))
                    .collect(Collectors.toList());

            outboundDetailRepository.saveAll(outboundDetails);
        });
    }

    /**
     * 창고관리자: 출고 상세 품목의 피킹 구역 배정 (ASSIGNED 상태에서만 가능).
     * 해당 구역의 가용 재고(Inventory.allocate)를 함께 확인/예약
     * 재배정인 경우 기존 구역의 할당을 먼저 되돌린다.
     */
    @Transactional
    public void assignSection(Long outboundDetailId, Long sectionId, User user) {
        OutboundDetail detail = outboundDetailRepository.findById(outboundDetailId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("outbound.detail.invalid")));

        Outbound outbound = detail.getOutbound();
        if (outbound.getStatus() != OutboundStatus.ASSIGNED) {
            throw new IllegalArgumentException(getMessage("outbound.status.not.allowed"));
        }

        validateWarehouseAccess(outbound.getWarehouse(), user);

        Section newSection = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("sectionId.invalid")));

        if (!newSection.getWarehouse().getId().equals(outbound.getWarehouse().getId())) {
            throw new IllegalArgumentException(getMessage("outbound.section.unauthorized"));
        }

        // 재배정이면 기존 구역의 재고 할당을 먼저 되돌린다.
        if (detail.getSection() != null) {
            inventoryRepository.findBySectionAndProductSKU(detail.getSection(), detail.getProductSKU())
                    .ifPresent(inventory -> inventory.deallocate(detail.getQuantity()));
        }

        // 새 구역의 가용 재고를 확인하고 할당
        Inventory inventory = inventoryRepository.findBySectionAndProductSKU(newSection, detail.getProductSKU())
                .orElseThrow(() -> new IllegalArgumentException(
                        getMessage("outbound.inventory.insufficient", 0, detail.getQuantity())));
        inventory.allocate(detail.getQuantity());

        detail.assignSection(newSection);
    }

    // 창고관리자: 구역 배정 초기화 (ASSIGNED 상태에서만). 할당 재고도 되돌린다.
    @Transactional
    public void clearSection(Long outboundDetailId, User user) {
        OutboundDetail detail = outboundDetailRepository.findById(outboundDetailId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("outbound.detail.invalid")));

        Outbound outbound = detail.getOutbound();
        if (outbound.getStatus() != OutboundStatus.ASSIGNED) {
            throw new IllegalArgumentException(getMessage("outbound.status.not.allowed"));
        }

        validateWarehouseAccess(outbound.getWarehouse(), user);

        if (detail.getSection() == null) {
            return;
        }

        inventoryRepository.findBySectionAndProductSKU(detail.getSection(), detail.getProductSKU())
                .ifPresent(inventory -> inventory.deallocate(detail.getQuantity()));

        detail.assignSection(null);
    }

    // 창고관리자: 모든 품목 구역 배정 완료 후 ASSIGNED → APPROVED
    @Transactional
    public void approveOutbound(Long outboundId, User user) {
        Outbound outbound = getOutbound(outboundId);
        validateWarehouseAccess(outbound.getWarehouse(), user);

        List<OutboundDetail> details = outboundDetailRepository.findByOutboundId(outboundId);
        if (details.stream().anyMatch(d -> d.getSection() == null)) {
            throw new IllegalArgumentException(getMessage("outbound.section.unassigned"));
        }

        outbound.approve();
    }

    // 창고관리자/작업자: 피킹 작업 시작 APPROVED → PICKING
    @Transactional
    public void startPicking(Long outboundId, User user) {
        Outbound outbound = getOutbound(outboundId);
        validateWarehouseAccess(outbound.getWarehouse(), user);
        outbound.startPicking();
    }

    /**
     * 피킹 완료 처리 PICKING → PACKING.
     * 이 시점에 실물이 구역에서 빠져나간다.
     * - Inventory: actualQuantity, allocatedQuantity 동시 차감
     * - Section: currentUsage 감소
     * - InventoryTransaction: SHIPMENT_COMPLETE로 기록
     */
    @Transactional
    public void completePicking(Long outboundId, User user) {
        Outbound outbound = getOutbound(outboundId);
        validateWarehouseAccess(outbound.getWarehouse(), user);

        List<OutboundDetail> details = outboundDetailRepository.findByOutboundId(outboundId);

        details.forEach(d -> {
            Section section = d.getSection();
            ProductSKU sku = d.getProductSKU();

            Inventory inventory = inventoryRepository.findBySectionAndProductSKU(section, sku)
                    .orElseThrow(() -> new IllegalArgumentException(getMessage("outbound.inventory.insufficient", 0, d.getQuantity())));

            int beforeQuantity = inventory.getActualQuantity();
            inventory.pick(d.getQuantity());
            int afterQuantity = inventory.getActualQuantity();

            // 구역 점유율 감소 (물건이 물리적으로 빠져나감)
            section.decreaseUsage(d.getQuantity());

            // 재고 거래 기록
            inventoryTransactionRepository.save(InventoryTransaction.of(
                    section, sku, TransactionType.SHIPMENT_COMPLETE,
                    d.getQuantity(), InventoryStatus.ALLOCATED,
                    beforeQuantity, afterQuantity,
                    "출고 피킹 완료 (출고 ID: " + outboundId + ")",
                    user
            ));
        });

        outbound.completePicking();
    }

    // 포장 완료 후 배송 출발 PACKING → SHIPPED
    @Transactional
    public void shipOutbound(Long outboundId, User user) {
        Outbound outbound = getOutbound(outboundId);
        validateWarehouseAccess(outbound.getWarehouse(), user);
        outbound.ship();
    }

    // 지점 최종 수령 확인 SHIPPED → DELIVERED
    @Transactional
    public void deliverOutbound(Long outboundId, User user) {
        Outbound outbound = getOutbound(outboundId);
        outbound.deliver();
    }

    private void validateWarehouseAccess(Warehouse warehouse, User user) {
        boolean isMyWarehouse = warehouseManagementRepository.findAllByUser(user).stream()
                .map(WarehouseManagement::getWarehouse)
                .anyMatch(myWarehouse -> myWarehouse.getId().equals(warehouse.getId()));
        if (!isMyWarehouse) {
            throw new IllegalArgumentException(getMessage("outbound.warehouse.unauthorized"));
        }
    }
}
