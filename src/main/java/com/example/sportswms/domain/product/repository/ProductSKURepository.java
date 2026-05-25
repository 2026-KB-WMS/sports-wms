package com.example.sportswms.domain.product.repository;

import com.example.sportswms.domain.product.entity.ProductSKU;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductSKURepository  extends JpaRepository<ProductSKU, Long> {
    Optional<ProductSKU> findById(String Id);
}
