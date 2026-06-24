package com.example.sportswms.domain.product.repository;

import com.example.sportswms.domain.product.entity.CategoryOptionMapping;
import com.example.sportswms.domain.product.entity.CategoryOptionMappingType;
import com.example.sportswms.domain.product.entity.OptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryOptionMappingRepository extends JpaRepository<CategoryOptionMapping, Long> {

    @Query("SELECT m.optionGroup FROM CategoryOptionMapping m WHERE m.category.id = :categoryId AND m.type = :type")
    List<OptionGroup> findOptionGroupsByCategoryIdAndType(@Param("categoryId") Long categoryId,
                                                          @Param("type") CategoryOptionMappingType type);
}
