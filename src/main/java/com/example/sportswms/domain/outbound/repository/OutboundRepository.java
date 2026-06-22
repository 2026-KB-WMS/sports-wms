package com.example.sportswms.domain.outbound.repository;

import com.example.sportswms.domain.order.entity.Store;
import com.example.sportswms.domain.outbound.entity.Outbound;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboundRepository extends JpaRepository<Outbound,Long> {
    List<Outbound> findAllByWarehouseIn(List<Warehouse> warehouses);

    // 점주가 관리하는 지점(Store)의 출고 내역. Outbound는 지점 단위로 생성되며 지점 정보는 상세를 통해 참조
    @Query("select distinct o from Outbound o " +
            "join o.outboundDetails d " +
            "where d.stockOrderDetail.store in :stores")
    List<Outbound> findAllByStoreIn(@Param("stores") List<Store> stores);
}