package com.example.sportswms.domain.outbound.repository;

import com.example.sportswms.domain.outbound.entity.OutboundDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboundDetailRepository extends JpaRepository<OutboundDetail,Long> {
    List<OutboundDetail> findByOutboundId(Long outboundId);
}
