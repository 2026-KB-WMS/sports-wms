package com.example.sportswms.domain.inbound.repository;

import com.example.sportswms.domain.inbound.entity.InboundDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboundDetailRepository extends JpaRepository<InboundDetail, Long> {
}
