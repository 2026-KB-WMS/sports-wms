package com.example.sportswms.domain.inbound.repository;

import com.example.sportswms.domain.inbound.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
