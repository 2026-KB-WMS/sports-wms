package com.example.sportswms.domain.outbound.repository;

import com.example.sportswms.domain.outbound.entity.Outbound;
import com.example.sportswms.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboundRepository extends JpaRepository<Outbound, Long> {

    @Query("SELECT o FROM Outbound o JOIN FETCH o.warehouse JOIN FETCH o.store")
    List<Outbound> findAllWithWarehouseAndStore();

    @Query("SELECT o FROM Outbound o JOIN FETCH o.warehouse JOIN FETCH o.store " +
           "JOIN WarehouseManagement wm ON wm.warehouse = o.warehouse " +
           "WHERE wm.user = :user")
    List<Outbound> findAllByWarehouseManager(@Param("user") User user);

    @Query("SELECT o FROM Outbound o JOIN FETCH o.warehouse JOIN FETCH o.store " +
           "JOIN StoreManagement sm ON sm.store = o.store " +
           "WHERE sm.user = :user")
    List<Outbound> findAllByStoreOwner(@Param("user") User user);
}