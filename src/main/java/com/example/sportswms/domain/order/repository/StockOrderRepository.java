package com.example.sportswms.domain.order.repository;

import com.example.sportswms.domain.order.entity.StockOrder;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockOrderRepository extends JpaRepository<StockOrder, Long> {
    List<StockOrder> findAllByWarehouseIn(List<Warehouse> warehouses);
}
