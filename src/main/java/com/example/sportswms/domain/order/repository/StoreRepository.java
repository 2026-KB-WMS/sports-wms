package com.example.sportswms.domain.order.repository;

import com.example.sportswms.domain.order.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
}
