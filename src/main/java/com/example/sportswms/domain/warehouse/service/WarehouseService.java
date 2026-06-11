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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        Section section = Section.of(dto, warehouse);
        sectionRepository.save(section);
    }

    @Transactional
    public void assignWarehouseManager(WarehouseAssignRequestDTO dto) {
        Warehouse warehouse = warehouseRepository.findById(dto.warehouseId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 창고 ID입니다."));

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원 ID입니다."));

        if (warehouseManagementRepository.existsByWarehouseAndUserAndManagementType(warehouse, user, dto.managementType())) {
            throw new IllegalStateException("이미 해당 창고에 동일한 권한으로 배정된 회원입니다.");
        }

        WarehouseManagement warehouseManagement = WarehouseManagement.of(warehouse, user, dto.managementType());
        warehouseManagementRepository.save(warehouseManagement);
    }
}
