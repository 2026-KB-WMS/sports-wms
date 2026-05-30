package com.example.sportswms.domain.product.service;

import com.example.sportswms.domain.product.dto.ProductCreateRequestDTO;
import com.example.sportswms.domain.product.entity.*;
import com.example.sportswms.domain.product.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSKURepository productSKURepository;
    private final OptionValueRepository optionValueRepository;
    private final OptionGroupRepository optionGroupRepository;

    public List<ProductSKU> getAllSKUs() {
        return productSKURepository.findAll();
    }
    public List<Category> getAllCategories() { return categoryRepository.findAll(); }
    public List<OptionGroup> getAllOptionGroups() { return optionGroupRepository.findAll(); }

    @Transactional
    public void createProduct(ProductCreateRequestDTO dto) {
        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리 ID입니다."));
        Product product = Product.of(dto, category);
        productRepository.save(product);

        ProductSKU sku = ProductSKU.of(dto, product);
        List<OptionValue> optionValues = optionValueRepository.findAllById(dto.optionValueIds());
        sku.addOptionValues(optionValues);
        productSKURepository.save(sku);
    }
}