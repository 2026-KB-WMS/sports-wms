package com.example.sportswms.domain.order.repository;

import com.example.sportswms.domain.order.entity.Store;
import com.example.sportswms.domain.order.entity.StoreManagement;
import com.example.sportswms.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StoreManagementRepository extends JpaRepository<StoreManagement, Long> {
    boolean existsByStoreAndUser(Store store, User user);
    List<StoreManagement> findByUserId(Long userId);
    List<StoreManagement> findAllByUser(User user);

    @Query("SELECT sm FROM StoreManagement sm JOIN FETCH sm.store JOIN FETCH sm.user")
    List<StoreManagement> findAllWithStoreAndUser();
}
