package com.example.sportswms.domain.inbound.repository;

import com.example.sportswms.domain.inbound.entity.Inbound;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboundRepository extends JpaRepository<Inbound, Long> {
}
