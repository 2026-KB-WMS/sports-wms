package com.example.sportswms.global.util;

import com.example.sportswms.domain.product.entity.*;
import com.example.sportswms.domain.product.repository.CategoryRepository;
import com.example.sportswms.domain.product.repository.OptionGroupRepository;
import com.example.sportswms.domain.product.repository.ProductRepository;
import com.example.sportswms.domain.product.repository.ProductSKURepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    // 도메인의 핵심이 되는 4개 Repository만 유지
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductSKURepository productSKURepository;
    private final OptionGroupRepository optionGroupRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // 데이터 중복 적재 방지
        if (categoryRepository.count() > 0) {
            return;
        }

        // ==========================================
        // 1. 카테고리(Category) 기본 생성
        // ==========================================
        Category racketCategory = new Category();
        racketCategory.setName("라켓");

        Category shoeCategory = new Category();
        shoeCategory.setName("신발");

        Category clothingCategory = new Category();
        clothingCategory.setName("의류");


        // ==========================================
        // 2. 옵션 마스터(OptionGroup & OptionValue) 생성
        // ==========================================
        // [옵션 그룹 1: 무게]
        OptionGroup weightGroup = new OptionGroup();
        weightGroup.setName("무게");

        OptionValue v4u = new OptionValue();
        v4u.setName("4U");
        v4u.setOptionGroup(weightGroup);

        OptionValue v5u = new OptionValue();
        v5u.setName("5U");
        v5u.setOptionGroup(weightGroup);

        weightGroup.getOptionValues().add(v4u);
        weightGroup.getOptionValues().add(v5u);
        optionGroupRepository.save(weightGroup);

        // [옵션 그룹 2: 그립 두께]
        OptionGroup gripGroup = new OptionGroup();
        gripGroup.setName("그립 두께");

        OptionValue vg5 = new OptionValue();
        vg5.setName("G5");
        vg5.setOptionGroup(gripGroup);

        OptionValue vg6 = new OptionValue();
        vg6.setName("G6");
        vg6.setOptionGroup(gripGroup);

        gripGroup.getOptionValues().add(vg5);
        gripGroup.getOptionValues().add(vg6);
        optionGroupRepository.save(gripGroup);

        // [옵션 그룹 3: 색상]
        OptionGroup colorGroup = new OptionGroup();
        colorGroup.setName("색상");

        OptionValue vBlack = new OptionValue();
        vBlack.setName("Black");

        vBlack.setOptionGroup(colorGroup);
        OptionValue vWhite = new OptionValue();
        vWhite.setName("White");

        vWhite.setOptionGroup(colorGroup);
        OptionValue vRed = new OptionValue();
        vRed.setName("Red");

        vRed.setOptionGroup(colorGroup);
        colorGroup.getOptionValues().add(vBlack);
        colorGroup.getOptionValues().add(vWhite);
        colorGroup.getOptionValues().add(vRed);
        optionGroupRepository.save(colorGroup);

        // [옵션 그룹 4: 사이즈]
        OptionGroup sizeGroup = new OptionGroup();
        sizeGroup.setName("사이즈");

        OptionValue v265 = new OptionValue();
        v265.setName("265mm");
        v265.setOptionGroup(sizeGroup);

        OptionValue v270 = new OptionValue();
        v270.setName("270mm");
        v270.setOptionGroup(sizeGroup);

        OptionValue vL = new OptionValue();
        vL.setName("L");
        vL.setOptionGroup(sizeGroup);

        OptionValue vXL = new OptionValue();
        vXL.setName("XL");
        vXL.setOptionGroup(sizeGroup);

        sizeGroup.getOptionValues().add(v265);
        sizeGroup.getOptionValues().add(v270);
        sizeGroup.getOptionValues().add(vL);
        sizeGroup.getOptionValues().add(vXL);
        optionGroupRepository.save(sizeGroup);

        // [옵션 그룹 5: 성별 구분]
        OptionGroup genderGroup = new OptionGroup();
        genderGroup.setName("성별 구분");

        OptionValue vMale = new OptionValue();
        vMale.setName("남성용");
        vMale.setOptionGroup(genderGroup);

        OptionValue vFemale = new OptionValue();
        vFemale.setName("여성용");
        vFemale.setOptionGroup(genderGroup);

        genderGroup.getOptionValues().add(vMale);
        genderGroup.getOptionValues().add(vFemale);
        optionGroupRepository.save(genderGroup);


        // ==========================================
        // 3. 카테고리별 매핑 (CategoryOptionMapping)
        // ==========================================
        // 라켓 -> 무게, 그립 두께, 색상 접근 가능하게 연결
        CategoryOptionMapping hqRacket1 = new CategoryOptionMapping();
        hqRacket1.setCategory(racketCategory);
        hqRacket1.setOptionGroup(weightGroup);

        CategoryOptionMapping hqRacket2 = new CategoryOptionMapping();
        hqRacket2.setCategory(racketCategory);
        hqRacket2.setOptionGroup(gripGroup);

        CategoryOptionMapping hqRacket3 = new CategoryOptionMapping();
        hqRacket3.setCategory(racketCategory);
        hqRacket3.setOptionGroup(colorGroup);

        racketCategory.getCategoryOptionMappings().add(hqRacket1);
        racketCategory.getCategoryOptionMappings().add(hqRacket2);
        racketCategory.getCategoryOptionMappings().add(hqRacket3);

        // 신발 -> 사이즈, 색상 접근 가능하게 연결
        CategoryOptionMapping hqShoe1 = new CategoryOptionMapping();
        hqShoe1.setCategory(shoeCategory);
        hqShoe1.setOptionGroup(sizeGroup);

        CategoryOptionMapping hqShoe2 = new CategoryOptionMapping();
        hqShoe2.setCategory(shoeCategory);
        hqShoe2.setOptionGroup(colorGroup);

        shoeCategory.getCategoryOptionMappings().add(hqShoe1);
        shoeCategory.getCategoryOptionMappings().add(hqShoe2);

        // 의류 -> 사이즈, 색상, 성별 구분 접근 가능하게 연결
        CategoryOptionMapping hqCloth1 = new CategoryOptionMapping();
        hqCloth1.setCategory(clothingCategory);
        hqCloth1.setOptionGroup(sizeGroup);

        CategoryOptionMapping hqCloth2 = new CategoryOptionMapping();
        hqCloth2.setCategory(clothingCategory);
        hqCloth2.setOptionGroup(colorGroup);

        CategoryOptionMapping hqCloth3 = new CategoryOptionMapping();
        hqCloth3.setCategory(clothingCategory);
        hqCloth3.setOptionGroup(genderGroup);

        clothingCategory.getCategoryOptionMappings().add(hqCloth1);
        clothingCategory.getCategoryOptionMappings().add(hqCloth2);
        clothingCategory.getCategoryOptionMappings().add(hqCloth3);

        // 카테고리 최종 저장 (영속성 전이로 매핑 테이블도 자동 save)
        categoryRepository.save(racketCategory);
        categoryRepository.save(shoeCategory);
        categoryRepository.save(clothingCategory);

        // ==========================================
        // 4. 테스트 상품(Product) 및 실제 SKU 데이터 주입
        // ==========================================
        // [라켓 등록]
        Product nanoflare = new Product();
        nanoflare.setName("나노플레어 700");
        nanoflare.setBrand("요넥스");
        nanoflare.setPrice(249000);
        nanoflare.setCategory(racketCategory);
        productRepository.save(nanoflare);

        ProductSKU sku1 = new ProductSKU();
        sku1.setProduct(nanoflare);
        sku1.setName("나노플레어 700 (4U/G5/Cyan)");
        sku1.setSkuCode("YONEX-NF700-4UG5-CYAN");

        // SKU에 실제 옵션 값 매핑 (4U, G5 매핑)
        OptionSKUMapping m1 = new OptionSKUMapping();
        m1.setProductSKU(sku1);
        m1.setOptionValue(v4u);
        sku1.getOptionSKUMappings().add(m1);

        OptionSKUMapping m2 = new OptionSKUMapping();
        m2.setProductSKU(sku1);
        m2.setOptionValue(vg5);
        sku1.getOptionSKUMappings().add(m2);

        productSKURepository.save(sku1);

        // [신발 등록 예시 데이터]
        Product aeroComfort = new Product();
        aeroComfort.setName("에어로 컴포트");
        aeroComfort.setBrand("요넥스");
        aeroComfort.setPrice(159000);
        aeroComfort.setCategory(shoeCategory);
        productRepository.save(aeroComfort);

        ProductSKU skuShoe = new ProductSKU();
        skuShoe.setProduct(aeroComfort);
        skuShoe.setName("에어로 컴포트 (265mm/White)");
        skuShoe.setSkuCode("YONEX-SH-AERO-265W");

        OptionSKUMapping m3 = new OptionSKUMapping();
        m3.setProductSKU(skuShoe);
        m3.setOptionValue(v265);
        skuShoe.getOptionSKUMappings().add(m3);

        OptionSKUMapping m4 = new OptionSKUMapping();
        m4.setProductSKU(skuShoe);
        m4.setOptionValue(vWhite);
        skuShoe.getOptionSKUMappings().add(m4);
        productSKURepository.save(skuShoe);
    }
}