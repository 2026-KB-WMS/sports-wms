package com.example.sportswms.domain.order.repository;

import com.example.sportswms.domain.order.entity.StockOrder;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockOrderRepository extends JpaRepository<StockOrder, Long> {
    List<StockOrder> findAllByWarehouseIn(List<Warehouse> warehouses);

    @Query("SELECT so FROM StockOrder so JOIN FETCH so.warehouse WHERE so.warehouse IN :warehouses")
    List<StockOrder> findAllByWarehouseInWithWarehouse(@Param("warehouses") List<Warehouse> warehouses);
}
