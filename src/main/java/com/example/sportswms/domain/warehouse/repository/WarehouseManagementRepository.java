package com.example.sportswms.domain.warehouse.repository;

import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import com.example.sportswms.domain.warehouse.entity.WarehouseManagement;
import com.example.sportswms.domain.warehouse.entity.WarehouseManagementType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseManagementRepository extends JpaRepository<WarehouseManagement, Long> {
    boolean existsByWarehouseAndUserAndManagementType(Warehouse warehouse, User user, WarehouseManagementType managementType);
}
