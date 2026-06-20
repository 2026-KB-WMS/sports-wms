package com.example.sportswms.domain.outbound.repository;

import com.example.sportswms.domain.outbound.entity.Outbound;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboundRepository extends JpaRepository<Outbound,Long> {
}
