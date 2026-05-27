package com.example.sportswms.domain.product.repository;

import com.example.sportswms.domain.product.entity.OptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OptionGroupRepository extends JpaRepository<OptionGroup, Long> {
}
