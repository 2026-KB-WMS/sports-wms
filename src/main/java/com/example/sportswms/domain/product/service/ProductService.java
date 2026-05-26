package com.example.sportswms.domain.product.service;

import com.example.sportswms.domain.product.entity.Product;
import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.product.repository.CategoryRepository;
import com.example.sportswms.domain.product.repository.OptionGroupRepository;
import com.example.sportswms.domain.product.repository.ProductRepository;
import com.example.sportswms.domain.product.repository.ProductSKURepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSKURepository productSKURepository;
    private final OptionGroupRepository optionGroupRepository;

    @Transactional(readOnly = true)
    public List<ProductSKU> getAllSKUs() {
        return productSKURepository.findAll();
    }
}