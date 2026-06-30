package com.example.sportswms.domain.warehouse.service;

import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.user.repository.UserRepository;
import com.example.sportswms.domain.warehouse.api.dto.SectionCreateRequestDTO;
import com.example.sportswms.domain.warehouse.api.dto.WarehouseAssignRequestDTO;
import com.example.sportswms.domain.warehouse.api.dto.WarehouseCreateRequestDTO;
import com.example.sportswms.domain.warehouse.entity.Section;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import com.example.sportswms.domain.warehouse.entity.WarehouseManagement;
import com.example.sportswms.domain.warehouse.repository.SectionRepository;
import com.example.sportswms.domain.warehouse.repository.WarehouseManagementRepository;
import com.example.sportswms.domain.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
    public List<Section> getAllSections() { return sectionRepository.findAllWithWarehouse(); }
    public List<WarehouseManagement> getAllWarehouseManagements() { return warehouseManagementRepository.findAllWithWarehouseAndUser(); }

    public List<Warehouse> findMyWarehouses(User user) {
        return warehouseManagementRepository.findAllByUserWithWarehouse(user).stream()
                .map(WarehouseManagement::getWarehouse)
                .collect(Collectors.toList());
    }

    @Transactional
    public Warehouse createWarehouse(WarehouseCreateRequestDTO dto) {
        Warehouse warehouse = Warehouse.from(dto);
        return warehouseRepository.save(warehouse);
    }

    @Transactional
    public Section createSection(SectionCreateRequestDTO dto) {
        Warehouse warehouse = warehouseRepository.findById(dto.warehouseId())
                .orElseThrow(() -> new IllegalArgumentException(getMessage("warehouseId.invalid")));

        if (sectionRepository.existsByNameAndWarehouse(dto.name(), warehouse)) {
            throw new IllegalArgumentException(getMessage("section.name.duplicate"));
        }

        warehouse.addSectionCapacity(dto.totalCapacity());
        String sectionCode = generateSectionCode(warehouse, dto);
        Section section = Section.of(dto, sectionCode, warehouse);
        return sectionRepository.save(section);
    }

    private String generateSectionCode(Warehouse warehouse, SectionCreateRequestDTO dto) {
        return warehouse.getId() + "-" + dto.name() + "-" + dto.sectionType().getCode();
    }

    @Transactional
    public WarehouseManagement assignWarehouseManager(WarehouseAssignRequestDTO dto) {
        Warehouse warehouse = warehouseRepository.findById(dto.warehouseId())
                .orElseThrow(() -> new IllegalArgumentException(getMessage("warehouseId.invalid")));
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException(getMessage("userId.invalid")));
        if (warehouseManagementRepository.existsByWarehouseAndUser(warehouse, user)) {
            throw new IllegalStateException(getMessage("management.assignment.duplicate"));
        }
        WarehouseManagement warehouseManagement = WarehouseManagement.of(warehouse, user, dto.managementType());
        return warehouseManagementRepository.save(warehouseManagement);
    }

    @Transactional
    public void deleteSection(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("section.id.invalid")));

        if (section.getCurrentUsage() > 0) {
            throw new IllegalStateException(getMessage("section.delete.in.use"));
        }

        Warehouse warehouse = section.getWarehouse();
        warehouse.deleteSectionCapacity(section.getTotalCapacity());

        sectionRepository.delete(section);
    }
}
