package com.example.sportswms.domain.outbound.service;

import com.example.sportswms.domain.inventory.entity.TransactionType;
import com.example.sportswms.domain.inventory.repository.InventoryRepository;
import com.example.sportswms.domain.inventory.service.InventoryService;
import com.example.sportswms.domain.order.entity.OrderDetailStatus;
import com.example.sportswms.domain.order.entity.StockOrder;
import com.example.sportswms.domain.order.entity.StockOrderDetail;
import com.example.sportswms.domain.order.entity.Store;
import com.example.sportswms.domain.order.repository.StockOrderDetailRepository;
import com.example.sportswms.domain.outbound.api.dto.OutboundDetailViewDTO;
import com.example.sportswms.domain.outbound.entity.Outbound;
import com.example.sportswms.domain.outbound.entity.OutboundDetail;
import com.example.sportswms.domain.outbound.entity.OutboundStatus;
import com.example.sportswms.domain.outbound.repository.OutboundDetailRepository;
import com.example.sportswms.domain.outbound.repository.OutboundRepository;
import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.user.entity.Role;
import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.warehouse.entity.Section;
import com.example.sportswms.domain.warehouse.entity.SectionType;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import com.example.sportswms.domain.warehouse.repository.SectionRepository;
import com.example.sportswms.global.exception.outbound.OutboundInvalidStatusException;
import com.example.sportswms.global.exception.outbound.OutboundNotFoundException;
import com.example.sportswms.global.exception.outbound.OutboundValidationException;
import com.example.sportswms.global.security.AccessValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OutboundService {
    private final OutboundRepository outboundRepository;
    private final OutboundDetailRepository outboundDetailRepository;
    private final SectionRepository sectionRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;
    private final StockOrderDetailRepository stockOrderDetailRepository;
    private final AccessValidator accessValidator;

    public Outbound getOutbound(Long outboundId) {
        return outboundRepository.findById(outboundId)
                .orElseThrow(OutboundNotFoundException::outbound);
    }

    public OutboundDetail getOutboundDetail(Long detailId) {
        return outboundDetailRepository.findById(detailId)
                .orElseThrow(OutboundNotFoundException::detail);
    }

    public void validateDetailBelongsToOutbound(Long outboundId, Long detailId) {
        OutboundDetail detail = outboundDetailRepository.findById(detailId)
                .orElseThrow(OutboundNotFoundException::detail);
        if (!detail.getOutbound().getId().equals(outboundId)) {
            throw OutboundNotFoundException.detail();
        }
    }


    // 본사관리자: 전체 출고 내역
    public List<Outbound> getAllOutbounds() {
        return outboundRepository.findAllWithWarehouseAndStore();
    }

    // 창고 관리자: 본인이 관리하는 창고들의 출고 내역
    public List<Outbound> findMyWarehousesOutbounds(User user) {
        return outboundRepository.findAllByWarehouseManager(user);
    }

    // 점주: 본인이 관리하는 지점(Store)의 출고 내역
    public List<Outbound> findMyStoreOutbounds(User user) {
        return outboundRepository.findAllByStoreOwner(user);
    }

    public List<OutboundDetail> getOutboundDetails(Long outboundId, User user) {
        Outbound outbound = getOutbound(outboundId);
        if (user.getRole() == Role.ROLE_WAREHOUSE_MANAGER) {
            accessValidator.validateWarehouseAccess(outbound.getWarehouse(), user);
        } else if (user.getRole() == Role.ROLE_USER) {
            accessValidator.validateStoreAccess(outbound.getStore(), user);
        }
        return outboundDetailRepository.findByOutboundIdWithSku(outboundId);
    }

    public List<OutboundDetailViewDTO.SectionOptionDTO> getAssignableSections(Warehouse warehouse, ProductSKU sku) {
        return inventoryRepository.findAllByProductSKUAndSection_Warehouse(sku, warehouse).stream()
                .filter(inventory -> inventory.getAvailableQuantity() > 0)
                .filter(inventory -> inventory.getSection().getSectionType() != SectionType.DAMAGED_ZONE)
                .map(inventory -> {
                    Section section = inventory.getSection();
                    return new OutboundDetailViewDTO.SectionOptionDTO(
                            section.getId(), section.getName(), section.getSectionCode(),
                            inventory.getAvailableQuantity());
                })
                .toList();
    }

    @Transactional
    public void createOutboundFromStockOrder(Warehouse warehouse, StockOrder stockOrder, List<StockOrderDetail> stockOrderDetails) {
        Map<Long, List<StockOrderDetail>> detailsByStoreId = stockOrderDetails.stream()
                .collect(Collectors.groupingBy(detail -> detail.getStore().getId()));

        detailsByStoreId.forEach((storeId, detailsForStore) -> {
            Store store = detailsForStore.get(0).getStore();
            Outbound outbound = Outbound.create(warehouse, store, stockOrder);
            outboundRepository.save(outbound);

            List<OutboundDetail> outboundDetails = detailsForStore.stream()
                    .map(detail -> OutboundDetail.from(outbound, detail))
                    .collect(Collectors.toList());

            outboundDetailRepository.saveAll(outboundDetails);
        });
    }

    /**
     * 창고 관리자: 출고 상세 품목의 피킹 구역 배정 (ASSIGNED 상태에서만 가능)
     * 해당 구역의 가용 재고(Inventory.allocate)를 함께 확인/예약
     * 재배정인 경우 기존 구역의 할당을 먼저 되돌린다.
     */
    @Transactional
    public void assignSection(Long outboundDetailId, Long sectionId, User user) {
        OutboundDetail detail = outboundDetailRepository.findById(outboundDetailId)
                .orElseThrow(OutboundNotFoundException::detail);

        Outbound outbound = detail.getOutbound();
        if (outbound.getStatus() != OutboundStatus.ASSIGNED) {
            throw OutboundInvalidStatusException.statusNotAllowed();
        }

        accessValidator.validateWarehouseAccess(outbound.getWarehouse(), user);

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(OutboundNotFoundException::section);

        if (section.getSectionType() == SectionType.DAMAGED_ZONE) {
            throw OutboundValidationException.damagedSectionNotAllowed();
        }

        if (!section.getWarehouse().getId().equals(outbound.getWarehouse().getId())) {
            throw OutboundValidationException.sectionUnauthorized();
        }

        // 재배정이면 기존 구역의 재고 할당을 먼저 되돌린다.
        if (detail.getSection() != null) {
            inventoryRepository.findBySectionAndProductSKU(detail.getSection(), detail.getProductSKU())
                    .ifPresent(inv -> inv.deallocate(detail.getQuantity()));
        }

        // 새 구역의 가용 재고를 확인하고 할당
        inventoryRepository.findBySectionAndProductSKU(section, detail.getProductSKU())
                .orElseThrow(() -> OutboundValidationException.inventoryInsufficient(0, detail.getQuantity()))
                .allocate(detail.getQuantity());

        detail.assignSection(section);
    }

    // 창고 관리자: 구역 배정 초기화 (ASSIGNED 상태에서만). 할당 재고도 되돌린다.
    @Transactional
    public void clearSection(Long outboundDetailId, User user) {
        OutboundDetail detail = outboundDetailRepository.findById(outboundDetailId)
                .orElseThrow(OutboundNotFoundException::detail);

        Outbound outbound = detail.getOutbound();
        if (outbound.getStatus() != OutboundStatus.ASSIGNED) {
            throw OutboundInvalidStatusException.statusNotAllowed();
        }

        accessValidator.validateWarehouseAccess(outbound.getWarehouse(), user);

        if (detail.getSection() == null) return;

        inventoryRepository.findBySectionAndProductSKU(detail.getSection(), detail.getProductSKU())
                .ifPresent(inv -> inv.deallocate(detail.getQuantity()));

        detail.assignSection(null);
    }

    // 창고 관리자: 모든 품목 구역 배정 완료 후 ASSIGNED → APPROVED
    @Transactional
    public void approveOutbound(Long outboundId, User user) {
        Outbound outbound = getOutbound(outboundId);
        accessValidator.validateWarehouseAccess(outbound.getWarehouse(), user);

        List<OutboundDetail> details = outboundDetailRepository.findByOutboundId(outboundId);
        if (details.stream().anyMatch(d -> d.getSection() == null)) {
            throw OutboundValidationException.sectionUnassigned();
        }

        outbound.approve();
    }

    // 창고 관리자/작업자: 피킹 작업 시작 APPROVED → PICKING
    @Transactional
    public void startPicking(Long outboundId, User user) {
        Outbound outbound = getOutbound(outboundId);
        accessValidator.validateWarehouseAccess(outbound.getWarehouse(), user);
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
        accessValidator.validateWarehouseAccess(outbound.getWarehouse(), user);

        // 상태 검증을 먼저 수행 — 재고 차감 전에 실패 -> 재고 중복 반영 방지
        if (outbound.getStatus() != OutboundStatus.PICKING) {
            throw OutboundInvalidStatusException.statusNotAllowed();
        }

        List<OutboundDetail> details = outboundDetailRepository.findByOutboundId(outboundId);
        details.forEach(d -> inventoryService.recordInventory(
                d.getSection(), d.getProductSKU(), TransactionType.SHIPMENT_COMPLETE,
                -d.getQuantity(), "출고 피킹 완료 (출고 ID: " + outboundId + ")", user));

        outbound.completePicking();
    }

    // 포장 완료 후 배송 출발 PACKING → SHIPPED. StockOrderDetail도 DELIVERING으로 변경
    @Transactional
    public void shipOutbound(Long outboundId, User user) {
        Outbound outbound = getOutbound(outboundId);
        accessValidator.validateWarehouseAccess(outbound.getWarehouse(), user);

        // 이 Outbound에 묶인 StockOrderDetail들을 DELIVERING으로 변경
        outboundDetailRepository.findByOutboundId(outboundId).stream()
                .map(OutboundDetail::getStockOrderDetail)
                .forEach(StockOrderDetail::startDelivering);

        outbound.ship();
    }

    // 점주: 배송 페이지에서 Outbound 단위로 수령 완료 처리 (SHIPPED → DELIVERED)
    // 해당 Outbound의 StockOrderDetail들을 COMPLETED로 변경
    // StockOrder에 포함된 모든 항목이 완료되면 StockOrder도 완료 처리
    @Transactional
    public void deliverOutbound(Long outboundId, User user) {
        Outbound outbound = getOutbound(outboundId);
        accessValidator.validateStoreAccess(outbound.getStore(), user);

        outboundDetailRepository.findByOutboundId(outboundId).stream()
                .map(OutboundDetail::getStockOrderDetail)
                .forEach(stockOrderDetail -> {
                    stockOrderDetail.complete();

                    StockOrder stockOrder = stockOrderDetail.getStockOrder();
                    boolean hasUncompletedDetail = stockOrderDetailRepository
                            .existsByStockOrderAndStatusNot(stockOrder, OrderDetailStatus.COMPLETED);
                    if (!hasUncompletedDetail) {
                        stockOrder.complete();
                    }
                });

        outbound.deliver();
    }
}
