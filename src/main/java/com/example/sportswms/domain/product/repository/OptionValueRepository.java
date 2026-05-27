package com.example.sportswms.domain.product.repository;

import com.example.sportswms.domain.product.entity.OptionValue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionValueRepository extends JpaRepository<OptionValue, Long> {
}