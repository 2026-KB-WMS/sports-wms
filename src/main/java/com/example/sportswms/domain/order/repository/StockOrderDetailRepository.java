package com.example.sportswms.domain.order.repository;

import com.example.sportswms.domain.order.entity.StockOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockOrderDetailRepository extends JpaRepository<StockOrderDetail, Long> {
}
