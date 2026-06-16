package com.example.sportswms.domain.warehouse.service;

import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.user.repository.UserRepository;
import com.example.sportswms.domain.warehouse.dto.SectionCreateRequestDTO;
import com.example.sportswms.domain.warehouse.dto.WarehouseAssignRequestDTO;
import com.example.sportswms.domain.warehouse.dto.WarehouseCreateRequestDTO;
import com.example.sportswms.domain.warehouse.entity.Section;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import com.example.sportswms.domain.warehouse.entity.WarehouseManagement;
import com.example.sportswms.domain.warehouse.repository.SectionRepository;
import com.example.sportswms.domain.warehouse.repository.WarehouseManagementRepository;
import com.example.sportswms.domain.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.sportswms.global.util.MessageUtils.getMessage;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final WarehouseManagementRepository warehouseManagementRepository;

    public List<Warehouse> getAllWarehouses() { return warehouseRepository.findAll(); }
    public List<Section> getAllSections() { return sectionRepository.findAll(); }
    public List<WarehouseManagement> getAllWarehouseManagements() { return warehouseManagementRepository.findAll(); }

    @Transactional
    public void createWarehouse(WarehouseCreateRequestDTO dto) {
        Warehouse warehouse = Warehouse.from(dto);
        warehouseRepository.save(warehouse);
    }

    @Transactional
    public void createSection(SectionCreateRequestDTO dto) {
        Warehouse warehouse = warehouseRepository.findById(dto.warehouseId())
                .orElseThrow(() -> new IllegalArgumentException(getMessage("warehouseId.invalid")));

        if (sectionRepository.existsByNameAndWarehouse(dto.name(), warehouse)) {
            throw new IllegalArgumentException(getMessage("section.name.duplicate"));
        }

        // 창고의 수용량 업데이트 및 검증
        warehouse.addSectionCapacity(dto.totalCapacity());

        // 구역 코드 생성: 창고id-sectiontype-구역이름
        String sectionCode = generateSectionCode(warehouse, dto);

        // 구역 생성
        Section section = Section.of(dto, sectionCode, warehouse);
        sectionRepository.save(section);
    }

    @Transactional
    public void deleteSection(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("sectionId.invalid")));

        if (section.getCurrentUsage() > 0) {
            throw new IllegalStateException(getMessage("section.delete.inUse"));
        }

        Warehouse warehouse = section.getWarehouse();
        warehouse.deleteSectionCapacity(section.getTotalCapacity());

        sectionRepository.delete(section);
    }

    @Transactional
    public void assignWarehouseManager(WarehouseAssignRequestDTO dto) {
        Warehouse warehouse = warehouseRepository.findById(dto.warehouseId())
                .orElseThrow(() -> new IllegalArgumentException(getMessage("warehouseId.invalid")));

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException(getMessage("user.invalid")));

        if (warehouseManagementRepository.existsByWarehouseAndUser(warehouse, user)) {
            throw new IllegalStateException(getMessage("management.assignment.duplicate"));
        }

        WarehouseManagement warehouseManagement = WarehouseManagement.of(warehouse, user, dto.managementType());
        warehouseManagementRepository.save(warehouseManagement);
    }

    private String generateSectionCode(Warehouse warehouse, SectionCreateRequestDTO dto) {
        return dto.sectionType().getCode() + "-" + dto.name().toUpperCase() + "-" + warehouse.getId();
    }
}