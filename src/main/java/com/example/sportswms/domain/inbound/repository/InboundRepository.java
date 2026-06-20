package com.example.sportswms.domain.inbound.repository;

import com.example.sportswms.domain.inbound.entity.Inbound;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InboundRepository extends JpaRepository<Inbound, Long> {
    List<Inbound> findAllByWarehouseIn(List<Warehouse> warehouses);
}
