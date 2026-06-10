package com.example.sportswms.domain.product.service;

import com.example.sportswms.domain.product.dto.SKUCreateRequestDTO;
import com.example.sportswms.domain.product.entity.*;
import com.example.sportswms.domain.product.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSKURepository productSKURepository;
    private final OptionValueRepository optionValueRepository;
    private final OptionGroupRepository optionGroupRepository;
    private final MessageSource messageSource;

    public List<ProductSKU> getAllSKUs() {
        return productSKURepository.findAll();
    }
    public List<OptionGroup> getAllOptionGroups() { return optionGroupRepository.findAll(); }
    public List<Product> getAllProducts() { return productRepository.findAll(); }

    @Transactional
    public void createSKU(SKUCreateRequestDTO dto) {
        Product product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품 ID입니다."));

        List<OptionValue> optionValues = optionValueRepository.findAllById(dto.optionValueIds());
        if (optionValues.size() != dto.optionValueIds().size()) {
            throw new IllegalArgumentException("선택된 옵션 중 존재하지 않는 옵션이 있습니다.");
        }

        // SKU 이름 생성: 상품명 (옵션명/옵션명/...)
        String optionNames = optionValues.stream()
                .map(OptionValue::getName)
                .collect(Collectors.joining("/"));
        String skuName = product.getName() + " (" + optionNames + ")";

        // SKU 코드 생성: 상품코드-옵션코드-옵션코드...
        String optionCodes = optionValues.stream()
                .map(OptionValue::getCode)
                .collect(Collectors.joining("-"));
        String skuCode = product.getCode() + "-" + optionCodes;

        // 생성된 이름과 코드로 ProductSKU 객체 생성
        ProductSKU sku = ProductSKU.of(product, skuName, skuCode);
        sku.addOptionValues(optionValues);
        productSKURepository.save(sku);
    }

    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
