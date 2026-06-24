package com.example.sportswms.domain.product.repository;

import com.example.sportswms.domain.product.entity.ProductSpec;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductSpecRepository extends JpaRepository<ProductSpec, Long> {
}
