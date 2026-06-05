package com.example.sportswms.domain.order.repository;

import com.example.sportswms.domain.order.entity.Store;
import com.example.sportswms.domain.order.entity.StoreManagement;
import com.example.sportswms.domain.order.entity.StoreManagementType;
import com.example.sportswms.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreManagementRepository extends JpaRepository<StoreManagement, Long> {
    boolean existsByStoreAndUserAndStoreManagementType(Store store, User user, StoreManagementType storeManagementType);
}
