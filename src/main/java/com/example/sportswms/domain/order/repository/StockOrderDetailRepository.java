package com.example.sportswms.domain.order.repository;

import com.example.sportswms.domain.order.entity.Store;
import com.example.sportswms.domain.order.entity.StockOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockOrderDetailRepository extends JpaRepository<StockOrderDetail, Long> {
    List<StockOrderDetail> findByStoreIn(List<Store> stores);
}
