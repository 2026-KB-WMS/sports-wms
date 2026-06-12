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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final WarehouseManagementRepository warehouseManagementRepository;
    private final MessageSource messageSource;

    public List<Warehouse> getAllWarehouses() { return warehouseRepository.findAll(); }
    public List<Section> getAllSections() { return sectionRepository.findAll(); }
    public List<User> getAllUsers() { return userRepository.findAll(); }
    public List<WarehouseManagement> getAllWarehouseManagements() { return warehouseManagementRepository.findAll(); }

    @Transactional
    public void createWarehouse(WarehouseCreateRequestDTO dto) {
        Warehouse warehouse = Warehouse.from(dto);
        warehouseRepository.save(warehouse);
    }

    @Transactional
    public void createSection(SectionCreateRequestDTO dto) {
        Warehouse warehouse = warehouseRepository.findById(dto.warehouseId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 창고 ID입니다."));
        warehouse.addSectionCapacity(dto.totalCapacity());
        Section section = Section.of(dto, warehouse);
        sectionRepository.save(section);
    }

    @Transactional
    public void assignWarehouseManager(WarehouseAssignRequestDTO dto) {
        Warehouse warehouse = warehouseRepository.findById(dto.warehouseId())
                .orElseThrow(() -> new IllegalArgumentException(getMessage("management.warehouseId.invalid")));

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException(getMessage("management.userId.invalid")));

        if (warehouseManagementRepository.existsByWarehouseAndUser(warehouse, user)) {
            throw new IllegalStateException(getMessage("management.assignment.duplicate"));
        }

        WarehouseManagement warehouseManagement = WarehouseManagement.of(warehouse, user, dto.managementType());
        warehouseManagementRepository.save(warehouseManagement);
    }

    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
