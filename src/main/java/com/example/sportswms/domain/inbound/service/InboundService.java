package com.example.sportswms.domain.inbound.service;

import com.example.sportswms.domain.inbound.dto.InboundDetailViewDTO;
import com.example.sportswms.domain.inbound.dto.InboundItemRequestDTO;
import com.example.sportswms.domain.inbound.dto.InboundRequestDTO;
import com.example.sportswms.domain.inbound.entity.Inbound;
import com.example.sportswms.domain.inbound.entity.InboundDetail;
import com.example.sportswms.domain.inbound.entity.InboundStatus;
import com.example.sportswms.domain.inbound.repository.InboundDetailRepository;
import com.example.sportswms.domain.inbound.repository.InboundRepository;
import com.example.sportswms.domain.inventory.entity.TransactionType;
import com.example.sportswms.domain.inventory.service.InventoryService;
import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.product.repository.ProductSKURepository;
import com.example.sportswms.domain.user.entity.Role;
import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.warehouse.entity.Section;
import com.example.sportswms.domain.warehouse.entity.SectionType;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import com.example.sportswms.domain.warehouse.repository.SectionRepository;
import com.example.sportswms.domain.warehouse.repository.WarehouseManagementRepository;
import com.example.sportswms.domain.warehouse.repository.WarehouseRepository;
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
    private final InventoryService inventoryService;
    private final WarehouseManagementRepository warehouseManagementRepository;

    public List<Inbound> getAllInbounds() { return inboundRepository.findAll(); }

    public List<Inbound> findMyWarehousesInbounds(User user) {
        return inboundRepository.findAllByWarehouseManager(user);
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

    // 정상품 구역 드롭다운 — DAMAGED_ZONE 제외
    public List<InboundDetailViewDTO.SectionOptionDTO> getAssignableSections(Warehouse warehouse) {
        return sectionRepository.findAllByWarehouse(warehouse).stream()
                .filter(s -> s.getSectionType() != SectionType.DAMAGED_ZONE)
                .map(this::toSectionOptionDTO)
                .toList();
    }

    // 불량품 구역 드롭다운 — DAMAGED_ZONE만
    public List<InboundDetailViewDTO.SectionOptionDTO> getDefectSections(Warehouse warehouse) {
        return sectionRepository.findAllByWarehouseAndSectionType(warehouse, SectionType.DAMAGED_ZONE).stream()
                .map(this::toSectionOptionDTO)
                .toList();
    }

    private InboundDetailViewDTO.SectionOptionDTO toSectionOptionDTO(Section section) {
        int pendingQuantity = inboundDetailRepository.sumQuantityBySectionAndInboundStatus(
                section, InboundStatus.INSPECTING);
        int effectiveRemaining = section.getRemainingCapacity() - pendingQuantity;
        return new InboundDetailViewDTO.SectionOptionDTO(
                section.getId(), section.getName(), section.getSectionCode(), effectiveRemaining);
    }

    @Transactional
    public void createInbound(InboundRequestDTO dto, User user) {
        validateNoDuplicateSku(dto);
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

    private void validateNoDuplicateSku(InboundRequestDTO dto) {
        long distinctSkuCount = dto.items().stream()
                .map(InboundItemRequestDTO::skuId)
                .distinct()
                .count();
        if (distinctSkuCount < dto.items().size()) {
            throw new IllegalArgumentException(getMessage("inbound.sku.duplicate"));
        }
    }

    @Transactional
    public void advanceInboundStatus(Long inboundId, InboundStatus nextStatus) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("inbound.invalid")));
        inbound.advanceStatus(nextStatus);
    }

    @Transactional
    public void startInspection(Long inboundId, User user) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("inbound.invalid")));
        validateWarehouseAccess(inbound.getWarehouse(), user);
        inbound.startInspection();
    }

    @Transactional
    public void recordDefect(Long inboundDetailId, int defectQuantity, User user) {
        InboundDetail detail = getInspectingDetail(inboundDetailId, user);
        detail.recordDefect(defectQuantity);
    }

    @Transactional
    public void resetDefect(Long inboundDetailId, User user) {
        InboundDetail detail = getInspectingDetail(inboundDetailId, user);
        detail.resetDefect();
    }

    @Transactional
    public void assignSection(Long inboundDetailId, Long sectionId, User user) {
        InboundDetail detail = getInspectingDetail(inboundDetailId, user);

        if (!detail.isDefectRecorded()) {
            throw new IllegalStateException("불량 수량 확정 후 정상 구역을 배정할 수 있습니다.");
        }

        Section newSection = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("sectionId.invalid")));

        validateSectionBelongsToWarehouse(newSection, detail.getInbound().getWarehouse());

        int pendingQuantity = inboundDetailRepository.sumQuantityBySectionAndInboundStatus(
                newSection, InboundStatus.INSPECTING);
        if (newSection.equals(detail.getSection())) {
            pendingQuantity -= detail.getNormalQuantity();
        }

        int effectiveRemaining = newSection.getRemainingCapacity() - pendingQuantity;
        if (effectiveRemaining < detail.getNormalQuantity()) {
            throw new IllegalArgumentException(
                    getMessage("inbound.section.capacity.exceeded", effectiveRemaining, detail.getNormalQuantity()));
        }

        detail.assignSection(newSection);
    }

    @Transactional
    public void clearSection(Long inboundDetailId, User user) {
        InboundDetail detail = getInspectingDetail(inboundDetailId, user);
        detail.assignSection(null);
    }

    @Transactional
    public void assignDefectSection(Long inboundDetailId, Long sectionId, User user) {
        InboundDetail detail = getInspectingDetail(inboundDetailId, user);

        if (detail.getDefectQuantity() <= 0) {
            throw new IllegalArgumentException("불량 수량이 없는 품목에는 불량 구역을 배정할 수 없습니다.");
        }

        Section defectSection = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("sectionId.invalid")));

        if (defectSection.getSectionType() != SectionType.DAMAGED_ZONE) {
            throw new IllegalArgumentException("불량 구역(DAMAGED_ZONE) 타입의 구역만 배정할 수 있습니다.");
        }
        validateSectionBelongsToWarehouse(defectSection, detail.getInbound().getWarehouse());

        int pendingQuantity = inboundDetailRepository.sumQuantityBySectionAndInboundStatus(
                defectSection, InboundStatus.INSPECTING);
        if (defectSection.equals(detail.getDefectSection())) {
            pendingQuantity -= detail.getDefectQuantity();
        }

        int effectiveRemaining = defectSection.getRemainingCapacity() - pendingQuantity;
        if (effectiveRemaining < detail.getDefectQuantity()) {
            throw new IllegalArgumentException(
                    getMessage("inbound.section.capacity.exceeded", effectiveRemaining, detail.getDefectQuantity()));
        }

        detail.assignDefectSection(defectSection);
    }

    @Transactional
    public void clearDefectSection(Long inboundDetailId, User user) {
        InboundDetail detail = getInspectingDetail(inboundDetailId, user);
        detail.assignDefectSection(null);
    }

    @Transactional
    public void completeInbound(Long inboundId, User user) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("inbound.invalid")));
        validateWarehouseAccess(inbound.getWarehouse(), user);

        List<InboundDetail> details = inboundDetailRepository.findByInboundId(inboundId);

        // 정상 구역 미배정 품목 확인
        if (details.stream().anyMatch(d -> d.getSection() == null)) {
            throw new IllegalArgumentException(getMessage("inbound.section.unassigned"));
        }
        // 불량 수량 있는데 불량 구역 미배정 품목 확인
        if (details.stream().anyMatch(InboundDetail::needsDefectSection)) {
            throw new IllegalArgumentException("불량 수량이 있는 품목에 불량 구역이 배정되지 않았습니다.");
        }

        details.forEach(d -> {
            Section section  = d.getSection();
            ProductSKU sku   = d.getProductSKU();
            int normalQty    = d.getNormalQuantity();
            int defectQty    = d.getDefectQuantity();

            if (normalQty > 0) {
                inventoryService.recordNewInventory(
                        section, sku, TransactionType.STACKING_COMPLETE,
                        normalQty, "입고 완료 (입고 ID: " + inboundId + ")", user);
            }

            if (defectQty > 0) {
                inventoryService.recordNewInventory(
                        d.getDefectSection(), sku, TransactionType.DEFECT_INBOUND,
                        defectQty, "불량 입고 (입고 ID: " + inboundId + ")", user);
            }
        });

        inbound.complete();
    }

    // 공통: INSPECTING 상태 검증 + 창고 권한 검증 후 InboundDetail 반환
    private InboundDetail getInspectingDetail(Long inboundDetailId, User user) {
        InboundDetail detail = inboundDetailRepository.findById(inboundDetailId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("inbound.detail.invalid")));
        if (detail.getInbound().getStatus() != InboundStatus.INSPECTING) {
            throw new IllegalArgumentException(getMessage("inbound.status.not.allowed"));
        }
        validateWarehouseAccess(detail.getInbound().getWarehouse(), user);
        return detail;
    }

    private void validateSectionBelongsToWarehouse(Section section, Warehouse warehouse) {
        if (!section.getWarehouse().getId().equals(warehouse.getId())) {
            throw new IllegalArgumentException(getMessage("inbound.section.unauthorized"));
        }
    }

    private void validateWarehouseAccess(Warehouse warehouse, User user) {
        if (!warehouseManagementRepository.existsByWarehouseAndUser(warehouse, user)) {
            throw new IllegalArgumentException(getMessage("warehouse.unauthorized"));
        }
    }
}
