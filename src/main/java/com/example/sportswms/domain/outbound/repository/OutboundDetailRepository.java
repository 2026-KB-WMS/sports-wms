package com.example.sportswms.domain.outbound.repository;

import com.example.sportswms.domain.outbound.entity.OutboundDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboundDetailRepository extends JpaRepository<OutboundDetail, Long> {

    @Query("SELECT d FROM OutboundDetail d JOIN FETCH d.productSKU WHERE d.outbound.id = :outboundId")
    List<OutboundDetail> findByOutboundIdWithSku(@Param("outboundId") Long outboundId);

    List<OutboundDetail> findByOutboundId(Long outboundId);
}
