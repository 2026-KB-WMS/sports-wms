package com.example.sportswms.domain.order.repository;

import com.example.sportswms.domain.order.entity.Store;
import com.example.sportswms.domain.order.entity.StoreManagement;
import com.example.sportswms.domain.order.entity.StoreManagementType;
import com.example.sportswms.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreManagementRepository extends JpaRepository<StoreManagement, Long> {
    boolean existsByStoreAndUser(Store store, User user);
    List<StoreManagement> findByUserId(Long userId);
}
