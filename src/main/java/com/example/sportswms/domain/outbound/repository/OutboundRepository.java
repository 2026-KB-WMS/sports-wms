package com.example.sportswms.domain.outbound.repository;

import com.example.sportswms.domain.outbound.entity.Outbound;
import com.example.sportswms.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboundRepository extends JpaRepository<Outbound, Long> {

    // 창고 관리자: WarehouseManagement를 조인해 본인 창고 출고 조회
    @Query("SELECT o FROM Outbound o " +
           "JOIN WarehouseManagement wm ON wm.warehouse = o.warehouse " +
           "WHERE wm.user = :user")
    List<Outbound> findAllByWarehouseManager(@Param("user") User user);

    // 점주: StoreManagement를 조인해 본인 지점 출고 조회
    @Query("SELECT o FROM Outbound o " +
           "JOIN StoreManagement sm ON sm.store = o.store " +
           "WHERE sm.user = :user")
    List<Outbound> findAllByStoreOwner(@Param("user") User user);
}