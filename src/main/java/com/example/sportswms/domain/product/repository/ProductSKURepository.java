package com.example.sportswms.domain.product.repository;

import com.example.sportswms.domain.product.entity.ProductSKU;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductSKURepository extends JpaRepository<ProductSKU, Long> {

    @Query("SELECT s FROM ProductSKU s JOIN FETCH s.product p JOIN FETCH p.brand JOIN FETCH p.category")
    List<ProductSKU> findAllWithProductBrandCategory();
}
