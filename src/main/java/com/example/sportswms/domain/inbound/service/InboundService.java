package com.example.sportswms.domain.inbound.service;

import com.example.sportswms.domain.inbound.api.dto.InboundDetailViewDTO;
import com.example.sportswms.domain.inbound.api.dto.InboundItemRequestDTO;
import com.example.sportswms.domain.inbound.api.dto.InboundRequestDTO;
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
import com.example.sportswms.domain.warehouse.repository.WarehouseRepository;
import com.example.sportswms.global.security.AccessValidator;
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
public class InboundService {
    private final InboundRepository inboundRepository;
    private final InboundDetailRepository inboundDetailRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductSKURepository productSKURepository;
    private final SectionRepository sectionRepository;
    private final InventoryService inventoryService;
    private final AccessValidator accessValidator;

    public List<Inbound> getAllInbounds() { return inboundRepository.findAllWithWarehouse(); }

    public List<Inbound> findMyWarehousesInbounds(User user) {
        return inboundRepository.findAllByWarehouseManager(user);
    }

    public List<InboundDetail> getInboundDetails(Long inboundId, User user) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("inbound.invalid")));
        if (user.getRole() != Role.ROLE_GENERAL_MANAGER) {
            accessValidator.validateWarehouseAccess(inbound.getWarehouse(), user);
        }
        return inboundDetailRepository.findByInboundIdWithSku(inboundId);
    }

    public Inbound getInbound(Long inboundId) {
        return inboundRepository.findById(inboundId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("inbound.invalid")));
    }

    public InboundDetail getInboundDetail(Long inboundDetailId) {
        return inboundDetailRepository.findById(inboundDetailId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("inbound.detail.invalid")));
    }

    public void validateDetailBelongsToInbound(Long inboundId, Long detailId) {
        InboundDetail detail = inboundDetailRepository.findById(detailId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("inbound.detail.invalid")));
        if (!detail.getInbound().getId().equals(inboundId)) {
            throw new IllegalArgumentException(getMessage("inbound.detail.invalid"));
        }
    }

    // 정상품 구역 드롭다운 — DAMAGED_ZONE 제외
    public List<InboundDetailViewDTO.SectionOptionDTO> getAssignableSections(Long inboundId, User user) {
        Inbound inbound = getInbound(inboundId);
        if (user.getRole() != Role.ROLE_GENERAL_MANAGER) {
            accessValidator.validateWarehouseAccess(inbound.getWarehouse(), user);
        }
        List<Section> sections = sectionRepository.findAllByWarehouse(inbound.getWarehouse()).stream()
                .filter(s -> s.getSectionType() != SectionType.DAMAGED_ZONE)
                .toList();
        return toSectionOptionDTOs(sections);
    }

    // 불량품 구역 드롭다운 — DAMAGED_ZONE만
    public List<InboundDetailViewDTO.SectionOptionDTO> getDefectSections(Long inboundId, User user) {
        Inbound inbound = getInbound(inboundId);
        if (user.getRole() != Role.ROLE_GENERAL_MANAGER) {
            accessValidator.validateWarehouseAccess(inbound.getWarehouse(), user);
        }
        List<Section> sections = sectionRepository.findAllByWarehouseAndSectionType(
                inbound.getWarehouse(), SectionType.DAMAGED_ZONE);
        return toSectionOptionDTOs(sections);
    }

    // 여러 구역의 pending 합계를 한 번의 GROUP BY 쿼리로 조회 (N+1 해결)
    private List<InboundDetailViewDTO.SectionOptionDTO> toSectionOptionDTOs(List<Section> sections) {
        if (sections.isEmpty()) return List.of();

        Map<Long, Integer> pendingBySectionId = inboundDetailRepository
                .sumQuantityBySectionsAndInboundStatus(sections, InboundStatus.INSPECTING)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue()
                ));

        return sections.stream()
                .map(section -> {
                    int pendingQuantity = pendingBySectionId.getOrDefault(section.getId(), 0);
                    int effectiveRemaining = section.getRemainingCapacity() - pendingQuantity;
                    return new InboundDetailViewDTO.SectionOptionDTO(
                            section.getId(), section.getName(), section.getSectionCode(), effectiveRemaining);
                })
                .toList();
    }

    @Transactional
    public Inbound createInbound(InboundRequestDTO dto, User user) {
        validateNoDuplicateSku(dto);
        Warehouse warehouse = warehouseRepository.findById(dto.warehouseId())
                .orElseThrow(() -> new IllegalArgumentException(getMessage("warehouseId.invalid")));
        accessValidator.validateWarehouseAccess(warehouse, user);

        Inbound inbound = Inbound.create(warehouse);
        inboundRepository.save(inbound);

        List<InboundDetail> details = dto.items().stream().map(itemDto -> {
            ProductSKU sku = productSKURepository.findById(itemDto.skuId())
                    .orElseThrow(() -> new IllegalArgumentException(getMessage("sku.invalid")));
            return InboundDetail.create(inbound, sku, itemDto.quantity());
        }).collect(Collectors.toList());

        inboundDetailRepository.saveAll(details);
        return inbound;
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
        accessValidator.validateWarehouseAccess(inbound.getWarehouse(), user);
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
            throw new IllegalStateException(getMessage("inbound.defect.assign.after.recorded"));
        }

        Section newSection = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("sectionId.invalid")));

        if (newSection.getSectionType() == SectionType.DAMAGED_ZONE) {
            throw new IllegalArgumentException(getMessage("inbound.section.damaged.not.allowed"));
        }

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
            throw new IllegalArgumentException(getMessage("inbound.defect.section.no.defect"));
        }

        Section defectSection = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("sectionId.invalid")));

        if (defectSection.getSectionType() != SectionType.DAMAGED_ZONE) {
            throw new IllegalArgumentException(getMessage("inbound.defect.section.type.invalid"));
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
        accessValidator.validateWarehouseAccess(inbound.getWarehouse(), user);

        List<InboundDetail> details = inboundDetailRepository.findByInboundId(inboundId);

        // 정상 구역 미배정 품목 확인
        if (details.stream().anyMatch(d -> d.getSection() == null)) {
            throw new IllegalArgumentException(getMessage("inbound.section.unassigned"));
        }
        // 불량 수량 있는데 불량 구역 미배정 품목 확인
        if (details.stream().anyMatch(InboundDetail::needsDefectSection)) {
            throw new IllegalArgumentException(getMessage("inbound.defect.section.unassigned"));
        }

        details.forEach(d -> {
            Section section  = d.getSection();
            ProductSKU sku   = d.getProductSKU();
            int normalQty    = d.getNormalQuantity();
            int defectQty    = d.getDefectQuantity();

            if (normalQty > 0) {
                inventoryService.recordInventory(
                        section, sku, TransactionType.STACKING_COMPLETE,
                        normalQty, getMessage("inbound.transaction.reason.complete", inboundId), user);
            }

            if (defectQty > 0) {
                inventoryService.recordInventory(
                        d.getDefectSection(), sku, TransactionType.DEFECT_INBOUND,
                        defectQty, getMessage("inbound.transaction.reason.defect", inboundId), user);
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
        accessValidator.validateWarehouseAccess(detail.getInbound().getWarehouse(), user);
        return detail;
    }

    private void validateSectionBelongsToWarehouse(Section section, Warehouse warehouse) {
        if (!section.getWarehouse().getId().equals(warehouse.getId())) {
            throw new IllegalArgumentException(getMessage("inbound.section.unauthorized"));
        }
    }
}
