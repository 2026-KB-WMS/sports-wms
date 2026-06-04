package com.example.sportswms.domain.order.repository;

import com.example.sportswms.domain.order.entity.StockOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockOrderRepository extends JpaRepository<StockOrder, Long> {
}
