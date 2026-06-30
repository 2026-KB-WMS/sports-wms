package com.example.sportswms.domain.order.repository;

import com.example.sportswms.domain.order.entity.OrderDetailStatus;
import com.example.sportswms.domain.order.entity.StockOrder;
import com.example.sportswms.domain.order.entity.StockOrderDetail;
import com.example.sportswms.domain.order.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockOrderDetailRepository extends JpaRepository<StockOrderDetail, Long> {

    @Query("SELECT d FROM StockOrderDetail d JOIN FETCH d.store JOIN FETCH d.productSKU")
    List<StockOrderDetail> findAllWithStoreAndSku();

    @Query("SELECT d FROM StockOrderDetail d JOIN FETCH d.store JOIN FETCH d.productSKU WHERE d.stockOrder = :stockOrder")
    List<StockOrderDetail> findAllByStockOrderWithStoreAndSku(@Param("stockOrder") StockOrder stockOrder);

    @Query("SELECT d FROM StockOrderDetail d JOIN FETCH d.store JOIN FETCH d.productSKU WHERE d.store IN :stores")
    List<StockOrderDetail> findByStoreInWithSku(@Param("stores") List<Store> stores);

    boolean existsByStockOrderAndStatusNot(StockOrder stockOrder, OrderDetailStatus status);

    @Query("SELECT d FROM StockOrderDetail d JOIN FETCH d.store JOIN FETCH d.productSKU WHERE d.orderGroupId = :groupId")
    List<StockOrderDetail> findAllByOrderGroupIdWithStoreAndSku(@Param("groupId") String orderGroupId);
}
