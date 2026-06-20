package com.example.sportswms.domain.outbound.repository;

import com.example.sportswms.domain.outbound.entity.OutboundDetail;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OutboundDetailRepository extends JpaRepository<OutboundDetail,Long> {
}
