package com.example.sportswms.domain.inbound.repository;

import com.example.sportswms.domain.inbound.entity.Inbound;
import com.example.sportswms.domain.inbound.entity.InboundDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InboundDetailRepository extends JpaRepository<InboundDetail, Long> {
    List<InboundDetail> findByInboundId(Long inboundId);
    boolean existsByInboundAndSectionIsNull(Inbound inbound);
}
