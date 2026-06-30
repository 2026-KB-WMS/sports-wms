package com.example.sportswms.domain.inbound.repository;

import com.example.sportswms.domain.inbound.entity.Inbound;
import com.example.sportswms.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InboundRepository extends JpaRepository<Inbound, Long> {

    @Query("SELECT i FROM Inbound i JOIN FETCH i.warehouse")
    List<Inbound> findAllWithWarehouse();

    @Query("SELECT i FROM Inbound i JOIN FETCH i.warehouse " +
           "JOIN WarehouseManagement wm ON wm.warehouse = i.warehouse " +
           "WHERE wm.user = :user")
    List<Inbound> findAllByWarehouseManager(@Param("user") User user);
}
