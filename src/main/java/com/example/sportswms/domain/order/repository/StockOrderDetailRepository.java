package com.example.sportswms.domain.order.repository;

import com.example.sportswms.domain.order.entity.StockOrder;
import com.example.sportswms.domain.order.entity.StockOrderDetail;
import com.example.sportswms.domain.order.entity.OrderDetailStatus;
import com.example.sportswms.domain.order.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockOrderDetailRepository extends JpaRepository<StockOrderDetail, Long> {
    List<StockOrderDetail> findAllByStockOrder(StockOrder stockOrder);
    List<StockOrderDetail> findByStoreIn(List<Store> stores);

    // 산하 StockOrderDetail 중 COMPLETED가 아닌 항목이 하나라도 있으면 true
    // StockOrder 완료 여부 판단에 사용
    boolean existsByStockOrderAndStatusNot(StockOrder stockOrder, OrderDetailStatus status);

    List<StockOrderDetail> findAllByOrderGroupId(String orderGroupId);
}
