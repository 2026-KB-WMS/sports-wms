package com.example.sportswms.domain.product.service;

import com.example.sportswms.domain.product.api.dto.BrandCreateRequestDTO;
import com.example.sportswms.domain.product.api.dto.ProductCreateRequestDTO;
import com.example.sportswms.domain.product.api.dto.SKUCreateRequestDTO;
import com.example.sportswms.domain.product.entity.*;
import com.example.sportswms.domain.product.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.sportswms.global.util.MessageUtils.getMessage;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductSKURepository productSKURepository;
    private final ProductSpecRepository productSpecRepository;
    private final OptionValueRepository optionValueRepository;
    private final OptionGroupRepository optionGroupRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryOptionMappingRepository categoryOptionMappingRepository;

    public List<ProductSKU> getAllSKUs() {
        return productSKURepository.findAllWithProductBrandCategory();
    }

    public List<OptionGroup> getAllOptionGroups() {
        return optionGroupRepository.findAll();
    }

    public List<Product> getAllProducts() {
        return productRepository.findAllWithBrandAndCategory();
    }

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /** 카테고리의 SKU 타입 옵션그룹 반환 — SKU 등록 폼 렌더링용 */
    public List<OptionGroup> getSkuOptionGroupsByProductId(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("product.invalid")));
        return categoryOptionMappingRepository.findOptionGroupsByCategoryIdAndType(
                product.getCategory().getId(), CategoryOptionMappingType.SKU);
    }

    /** 카테고리의 SPEC 타입 옵션그룹 반환 — 상품 등록 폼 렌더링용 */
    public List<OptionGroup> getSpecOptionGroupsByCategoryId(Long categoryId) {
        return categoryOptionMappingRepository.findOptionGroupsByCategoryIdAndType(
                categoryId, CategoryOptionMappingType.SPEC);
    }

    @Transactional
    public Brand createBrand(BrandCreateRequestDTO dto) {
        return brandRepository.save(Brand.of(dto.name(), dto.code()));
    }

    @Transactional
    public Product createProduct(ProductCreateRequestDTO dto) {
        Brand brand = brandRepository.findById(dto.brandId())
                .orElseThrow(() -> new IllegalArgumentException(getMessage("brand.invalid")));
        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new IllegalArgumentException(getMessage("product.category.required")));

        Product product = productRepository.save(Product.of(dto, brand, category));

        List<Long> specIds = dto.specOptionValueIds() != null ? dto.specOptionValueIds() : Collections.emptyList();
        if (!specIds.isEmpty()) {
            List<OptionValue> specValues = optionValueRepository.findAllById(specIds);

            List<OptionGroup> requiredSpecGroups = categoryOptionMappingRepository
                    .findOptionGroupsByCategoryIdAndType(category.getId(), CategoryOptionMappingType.SPEC);
            Set<Long> selectedGroupIds = specValues.stream()
                    .map(v -> v.getOptionGroup().getId())
                    .collect(Collectors.toSet());
            boolean allCovered = requiredSpecGroups.stream()
                    .allMatch(g -> selectedGroupIds.contains(g.getId()));
            if (!allCovered) {
                throw new IllegalArgumentException(getMessage("product.spec.incomplete"));
            }

            List<ProductSpec> specs = specValues.stream()
                    .map(v -> ProductSpec.of(product, v))
                    .collect(Collectors.toList());
            productSpecRepository.saveAll(specs);
        }

        return product;
    }

    @Transactional
    public ProductSKU createSKU(SKUCreateRequestDTO dto) {
        Product product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new IllegalArgumentException(getMessage("product.invalid")));

        List<OptionValue> optionValues = optionValueRepository.findAllById(dto.optionValueIds());
        if (optionValues.size() != dto.optionValueIds().size()) {
            throw new IllegalArgumentException(getMessage("option.invalid"));
        }

        List<OptionGroup> requiredSkuGroups = categoryOptionMappingRepository
                .findOptionGroupsByCategoryIdAndType(product.getCategory().getId(), CategoryOptionMappingType.SKU);
        Set<Long> selectedGroupIds = optionValues.stream()
                .map(v -> v.getOptionGroup().getId())
                .collect(Collectors.toSet());
        boolean allGroupsCovered = requiredSkuGroups.stream()
                .allMatch(g -> selectedGroupIds.contains(g.getId()));
        if (!allGroupsCovered) {
            throw new IllegalArgumentException(getMessage("sku.option.group.incomplete"));
        }

        String skuName = generateSKUName(product, optionValues);
        String skuCode = generateSKUCode(product, optionValues);

        ProductSKU sku = ProductSKU.of(product, skuName, skuCode);
        sku.addOptionValues(optionValues);
        return productSKURepository.save(sku);
    }

    private String generateSKUName(Product product, List<OptionValue> optionValues) {
        String optionNames = optionValues.stream()
                .map(OptionValue::getName)
                .collect(Collectors.joining("/"));
        return product.getName() + " (" + optionNames + ")";
    }

    private String generateSKUCode(Product product, List<OptionValue> optionValues) {
        String optionCodes = optionValues.stream()
                .map(OptionValue::getCode)
                .collect(Collectors.joining("-"));
        return product.getCode() + "-" + optionCodes;
    }
}
