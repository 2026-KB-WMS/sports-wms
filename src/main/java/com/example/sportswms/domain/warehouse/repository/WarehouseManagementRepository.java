package com.example.sportswms.domain.warehouse.repository;

import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import com.example.sportswms.domain.warehouse.entity.WarehouseManagement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarehouseManagementRepository extends JpaRepository<WarehouseManagement, Long> {
    boolean existsByWarehouseAndUser(Warehouse warehouse, User user);
    List<WarehouseManagement> findAllByUser(User user);
}