package com.example.sportswms.domain.product.repository;

import com.example.sportswms.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByCode(String code);

    @Query("SELECT p FROM Product p JOIN FETCH p.brand JOIN FETCH p.category")
    List<Product> findAllWithBrandAndCategory();
}
