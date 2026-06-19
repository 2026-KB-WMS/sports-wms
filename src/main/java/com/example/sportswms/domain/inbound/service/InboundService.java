package com.example.sportswms.domain.inbound.service;

import com.example.sportswms.domain.inventory.entity.Inventory;
import com.example.sportswms.domain.inventory.repository.InventoryRepository;
import com.example.sportswms.domain.inbound.dto.InboundDetailViewDTO;
import com.example.sportswms.domain.inbound.dto.InboundRequestDTO;
import com.example.sportswms.domain.inbound.entity.Inbound;
import com.example.sportswms.domain.inbound.entity.InboundDetail;
import com.example.sportswms.domain.inbound.entity.InboundStatus;
import com.example.sportswms.domain.inbound.repository.InboundDetailRepository;
import com.example.sportswms.domain.inbound.repository.InboundRepository;
import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.product.repository.ProductSKURepository;
import com.example.sportswms.domain.user.entity.Role;
import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.warehouse.entity.Section;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import com.example.sportswms.domain.warehouse.repository.SectionRepository;
import com.example.sportswms.domain.warehouse.repository.WarehouseRepository;
import com.example.sportswms.domain.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.sportswms.global.util.MessageUtils.getMessage;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InboundService {
    private final InboundRepository inboundRepository;
    private final InboundDetailRepository inboundDetailRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductSKURepository productSKURepository;
    private final SectionRepository sectionRepository;
    private final WarehouseService warehouseService;
    private final InventoryRepository inventoryRepository;

    public List<Inbound> getAllInbounds() { return inboundRepository.findAll(); }

    public List<Inbound> findMyWarehousesInbounds(User user) {
        List<Warehouse> myWarehouses = warehouseService.findMyWarehouses(user);
        if (myWarehouses.isEmpty()) {
            return List.of();
        }
        return inboundRepository.findAllByWarehouseIn(myWarehouses);
    }

    public List<InboundDetail> getInboundDetails(Long inboundId, User user) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("inbound.invalid")));

        if (user.getRole() != Role.ROLE_GENERAL_MANAGER) {
            validateWarehouseAccess(inbound.getWarehouse(), user);
        }

        return inboundDetailRepository.findByInboundId(inboundId);
    }

    public Inbound getInbound(Long inboundId) {
        return inboundRepository.findById(inboundId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("inbound.invalid")));
    }

    // 검수 중인 입고에서 구역 배정 드롭다운에 보여줄 구역별 실시간 잔여 수용량을 계산한다.
    // effectiveRemaining = currentUsage 기준 잔여 - 검수 중인 다른 입고들이 해당 구역에 이미 배정한 수량 합계
    public List<InboundDetailViewDTO.SectionOptionDTO> getAssignableSections(Warehouse warehouse) {
        return warehouseService.findSectionsByWarehouse(warehouse).stream()
                .map(section -> {
                    int pendingQuantity = inboundDetailRepository.sumQuantityBySectionAndInboundStatus(
                            section, InboundStatus.INSPECTING);
                    int effectiveRemaining = section.getRemainingCapacity() - pendingQuantity;
                    return new InboundDetailViewDTO.SectionOptionDTO(
                            section.getId(), section.getName(), section.getSectionCode(), effectiveRemaining);
                })
                .toList();
    }

    @Transactional
    public void createInbound(InboundRequestDTO dto, User user) {
        Warehouse warehouse = warehouseRepository.findById(dto.warehouseId())
                .orElseThrow(() -> new IllegalArgumentException(getMessage("warehouseId.invalid")));

        validateWarehouseAccess(warehouse, user);

        Inbound inbound = Inbound.create(warehouse);
        inboundRepository.save(inbound);

        List<InboundDetail> details = dto.items().stream().map(itemDto -> {
            ProductSKU sku = productSKURepository.findById(itemDto.skuId())
                    .orElseThrow(() -> new IllegalArgumentException(getMessage("sku.invalid")));
            return InboundDetail.create(inbound, sku, itemDto.quantity());
        }).collect(Collectors.toList());

        inboundDetailRepository.saveAll(details);
    }

    // 본사 관리자가 입고 상태를 다음 단계로 진행 (PENDING → RECEIVED → DELIVERING → DELIVERED)
    @Transactional
    public void advanceInboundStatus(Long inboundId, InboundStatus nextStatus) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("inbound.invalid")));
        inbound.advanceStatus(nextStatus);
    }


    // 창고 관리자가 배송 완료된 입고를 검수 시작 상태로 전환 (DELIVERED → INSPECTING)
    @Transactional
    public void startInspection(Long inboundId, User user) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("inbound.invalid")));
        validateWarehouseAccess(inbound.getWarehouse(), user);
        inbound.startInspection();
    }

    // 창고 관리자가 특정 품목의 구역 배정을 초기화한다 (INSPECTING 상태에서만 가능)
    @Transactional
    public void clearSection(Long inboundDetailId, User user) {
        InboundDetail detail = inboundDetailRepository.findById(inboundDetailId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("inbound.detail.invalid")));

        Inbound inbound = detail.getInbound();
        if (inbound.getStatus() != InboundStatus.INSPECTING) {
            throw new IllegalArgumentException(getMessage("inbound.status.not.allowed"));
        }

        validateWarehouseAccess(inbound.getWarehouse(), user);

        if (detail.getSection() == null) {
            return; // 이미 미배정 상태면 아무것도 하지 않음
        }

        detail.assignSection(null);
    }

    // 창고 관리자가 입고 상세 품목에 구역을 배정 (INSPECTING 상태)
    // currentUsage(확정 재고) + 현재 검수 중인 배정 수량 합계로 실시간 잔여 용량을 계산해 초과 배정을 방지한다.
    @Transactional
    public void assignSection(Long inboundDetailId, Long sectionId, User user) {
        InboundDetail detail = inboundDetailRepository.findById(inboundDetailId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("inbound.detail.invalid")));

        Inbound inbound = detail.getInbound();
        if (inbound.getStatus() != InboundStatus.INSPECTING) {
            throw new IllegalArgumentException(getMessage("inbound.status.not.allowed"));
        }

        validateWarehouseAccess(inbound.getWarehouse(), user);

        Section newSection = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("sectionId.invalid")));

        if (!newSection.getWarehouse().getId().equals(inbound.getWarehouse().getId())) {
            throw new IllegalArgumentException(getMessage("inbound.section.unauthorized"));
        }

        // 해당 구역에 검수 중인 다른 입고들에서 이미 배정된 수량 합계
        int pendingQuantity = inboundDetailRepository.sumQuantityBySectionAndInboundStatus(
                newSection, InboundStatus.INSPECTING);

        // 재배정의 경우 현재 품목이 기존에 같은 구역에 배정돼 있으면 중복 합산되므로 제외
        if (newSection.equals(detail.getSection())) {
            pendingQuantity -= detail.getQuantity();
        }

        int effectiveRemaining = newSection.getRemainingCapacity() - pendingQuantity;
        if (effectiveRemaining < detail.getQuantity()) {
            throw new IllegalArgumentException(
                    getMessage("inbound.section.capacity.exceeded", effectiveRemaining, detail.getQuantity()));
        }

        detail.assignSection(newSection);
    }

    // 모든 품목에 구역이 배정되면 입고 완료 처리 (INSPECTING → COMPLETED)
    // 입고 완료 확정 시점에 각 구역의 currentUsage 및 재고 테이블을 반영
    @Transactional
    public void completeInbound(Long inboundId, User user) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("inbound.invalid")));
        validateWarehouseAccess(inbound.getWarehouse(), user);

        List<InboundDetail> details = inboundDetailRepository.findByInboundId(inboundId);

        if (details.stream().anyMatch(d -> d.getSection() == null)) {
            throw new IllegalArgumentException(getMessage("inbound.section.unassigned"));
        }

        details.forEach(d -> {
            Section section = d.getSection();

            // 구역 currentUsage 업데이트
            section.increaseUsage(d.getQuantity());

            // 재고 upsert: 해당 구역+SKU 재고가 있으면 수량 추가, 없으면 신규 생성
            inventoryRepository.findBySectionAndProductSKU(section, d.getProductSKU())
                    .ifPresentOrElse(
                            inventory -> inventory.addQuantity(d.getQuantity()),
                            () -> inventoryRepository.save(Inventory.create(section, d.getProductSKU(), d.getQuantity()))
                    );
        });

        inbound.complete();
    }

    private void validateWarehouseAccess(Warehouse warehouse, User user) {
        boolean isMyWarehouse = warehouseService.findMyWarehouses(user).stream()
                .anyMatch(myWarehouse -> myWarehouse.getId().equals(warehouse.getId()));
        if (!isMyWarehouse) {
            throw new IllegalArgumentException(getMessage("inbound.warehouse.unauthorized"));
        }
    }
}
