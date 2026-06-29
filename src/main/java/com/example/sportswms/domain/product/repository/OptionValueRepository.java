package com.example.sportswms.domain.product.repository;

import com.example.sportswms.domain.product.entity.OptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OptionValueRepository extends JpaRepository<OptionValue, Long> {

    @Query("SELECT ov FROM OptionValue ov WHERE ov.optionGroup.name = :groupName")
    List<OptionValue> findByOptionGroupName(@Param("groupName") String groupName);
}
