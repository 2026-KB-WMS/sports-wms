package com.example.sportswms.domain.product.repository;

import com.example.sportswms.domain.product.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findByCode(String code);
}
