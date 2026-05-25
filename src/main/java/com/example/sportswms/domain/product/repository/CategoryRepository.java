package com.example.sportswms.domain.product.repository;

import com.example.sportswms.domain.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository  extends JpaRepository<Category, Long> {
    Optional<Category> findById(String Id);

}
