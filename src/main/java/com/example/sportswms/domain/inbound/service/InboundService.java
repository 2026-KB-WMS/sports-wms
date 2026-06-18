package com.example.sportswms.domain.inbound.service;

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

    // 창고 관리자가 입고 상세 품목에 구역을 배정 (INSPECTING 상태)
    @Transactional
    public void assignSection(Long inboundDetailId, Long sectionId, User user) {
        InboundDetail detail = inboundDetailRepository.findById(inboundDetailId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("inbound.detail.invalid")));

        Inbound inbound = detail.getInbound();
        if (inbound.getStatus() != InboundStatus.INSPECTING) {
            throw new IllegalArgumentException(getMessage("inbound.status.not.allowed"));
        }

        validateWarehouseAccess(inbound.getWarehouse(), user);

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("sectionId.invalid")));

        // 구역이 해당 창고 소속인지 검증
        if (!section.getWarehouse().getId().equals(inbound.getWarehouse().getId())) {
            throw new IllegalArgumentException(getMessage("inbound.section.unauthorized"));
        }

        detail.assignSection(section);
    }

    // 모든 품목에 구역이 배정되면 입고 완료 처리 (INSPECTING → COMPLETED)
    @Transactional
    public void completeInbound(Long inboundId, User user) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("inbound.invalid")));
        validateWarehouseAccess(inbound.getWarehouse(), user);

        boolean hasUnassigned = inboundDetailRepository.findByInboundId(inboundId).stream()
                .anyMatch(detail -> detail.getSection() == null);

        if (hasUnassigned) {
            throw new IllegalArgumentException(getMessage("inbound.section.unassigned"));
        }

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
