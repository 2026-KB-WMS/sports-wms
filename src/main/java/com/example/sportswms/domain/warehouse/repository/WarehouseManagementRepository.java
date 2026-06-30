package com.example.sportswms.domain.warehouse.repository;

import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import com.example.sportswms.domain.warehouse.entity.WarehouseManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WarehouseManagementRepository extends JpaRepository<WarehouseManagement, Long> {
    boolean existsByWarehouseAndUser(Warehouse warehouse, User user);
    List<WarehouseManagement> findAllByUser(User user);

    @Query("SELECT wm FROM WarehouseManagement wm JOIN FETCH wm.warehouse JOIN FETCH wm.user")
    List<WarehouseManagement> findAllWithWarehouseAndUser();

    @Query("SELECT wm FROM WarehouseManagement wm JOIN FETCH wm.warehouse WHERE wm.user = :user")
    List<WarehouseManagement> findAllByUserWithWarehouse(@Param("user") User user);
}