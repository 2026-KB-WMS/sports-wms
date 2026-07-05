package com.example.sportswms.global.init;

import com.example.sportswms.domain.product.entity.Category;
import com.example.sportswms.domain.product.entity.CategoryOptionMapping;
import com.example.sportswms.domain.product.entity.OptionGroup;
import com.example.sportswms.domain.product.repository.CategoryOptionMappingRepository;
import com.example.sportswms.domain.product.repository.CategoryRepository;
import com.example.sportswms.domain.product.repository.OptionGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "wms.init.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements ApplicationRunner {

    private final OptionGroupRepository optionGroupRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryOptionMappingRepository categoryOptionMappingRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (optionGroupRepository.count() > 0 || categoryRepository.count() > 0) {
            log.info("[DataInitializer] 초기 데이터가 이미 존재합니다. 스킵합니다.");
            return;
        }

        log.info("[DataInitializer] 배드민턴 용품 마스터 데이터 삽입 시작.");

        // ── OptionGroup & OptionValue ──────────────────────────────────────────

        OptionGroup weight = OptionGroup.of("무게");
        weight.addOptionValue("2U", "2U");   // 90g 이상
        weight.addOptionValue("3U", "3U");   // 85~89g
        weight.addOptionValue("4U", "4U");   // 80~84g
        weight.addOptionValue("5U", "5U");   // 75~79g

        OptionGroup gripSize = OptionGroup.of("그립 사이즈");
        gripSize.addOptionValue("G4", "G4"); // 굵음
        gripSize.addOptionValue("G5", "G5"); // 보통
        gripSize.addOptionValue("G6", "G6"); // 가늘음

        OptionGroup flex = OptionGroup.of("샤프트 경도");
        flex.addOptionValue("Extra Stiff", "ES");
        flex.addOptionValue("Stiff", "ST");
        flex.addOptionValue("Medium", "MD");
        flex.addOptionValue("Flexible", "FL");

        OptionGroup balance = OptionGroup.of("라켓 밸런스");
        balance.addOptionValue("헤드헤비", "HH");
        balance.addOptionValue("이븐밸런스", "EB");
        balance.addOptionValue("헤드라이트", "HL");

        OptionGroup shuttleType = OptionGroup.of("셔틀콕 타입");
        shuttleType.addOptionValue("천연 깃털", "NAT");
        shuttleType.addOptionValue("합성 깃털", "SYN");
        shuttleType.addOptionValue("나일론", "NYL");

        OptionGroup shuttleSpeed = OptionGroup.of("셔틀콕 속도");
        shuttleSpeed.addOptionValue("75", "75");
        shuttleSpeed.addOptionValue("76", "76");
        shuttleSpeed.addOptionValue("77", "77");
        shuttleSpeed.addOptionValue("78", "78");

        OptionGroup shoeSize = OptionGroup.of("신발 사이즈");
        shoeSize.addOptionValue("230", "230");
        shoeSize.addOptionValue("235", "235");
        shoeSize.addOptionValue("240", "240");
        shoeSize.addOptionValue("245", "245");
        shoeSize.addOptionValue("250", "250");
        shoeSize.addOptionValue("255", "255");
        shoeSize.addOptionValue("260", "260");
        shoeSize.addOptionValue("265", "265");
        shoeSize.addOptionValue("270", "270");
        shoeSize.addOptionValue("275", "275");
        shoeSize.addOptionValue("280", "280");
        shoeSize.addOptionValue("285", "285");

        OptionGroup clothingSize = OptionGroup.of("의류 사이즈");
        clothingSize.addOptionValue("XS", "XS");
        clothingSize.addOptionValue("S", "S");
        clothingSize.addOptionValue("M", "M");
        clothingSize.addOptionValue("L", "L");
        clothingSize.addOptionValue("XL", "XL");
        clothingSize.addOptionValue("2XL", "2XL");

        OptionGroup color = OptionGroup.of("색상");
        color.addOptionValue("블랙", "BK");
        color.addOptionValue("화이트", "WH");
        color.addOptionValue("레드", "RD");
        color.addOptionValue("블루", "BL");
        color.addOptionValue("네이비", "NV");
        color.addOptionValue("옐로우", "YL");
        color.addOptionValue("그린", "GR");

        OptionGroup bagSize = OptionGroup.of("가방 타입");
        bagSize.addOptionValue("라켓백 (1~2자루)", "BAG-S");
        bagSize.addOptionValue("보스턴백 (3~6자루)", "BAG-M");
        bagSize.addOptionValue("투어백 (9~12자루)", "BAG-L");

        optionGroupRepository.saveAll(List.of(
                weight, gripSize, flex, balance,
                shuttleType, shuttleSpeed,
                shoeSize,
                clothingSize,
                color,
                bagSize
        ));

        // ── Category ──────────────────────────────────────────────────────────

        Category racket   = Category.of("라켓");
        Category shuttle  = Category.of("셔틀콕");
        Category shoes    = Category.of("신발");
        Category clothing = Category.of("의류");
        Category bag      = Category.of("가방");
        Category string   = Category.of("스트링");
        Category grip     = Category.of("그립");
        Category guard    = Category.of("보호대");

        categoryRepository.saveAll(List.of(racket, shuttle, shoes, clothing, bag, string, grip, guard));

        // ── CategoryOptionMapping ──────────────────────────────────────────────

        categoryOptionMappingRepository.saveAll(List.of(
                // 라켓: 무게·그립 사이즈·색상 → SKU / 샤프트 경도 → SPEC
                CategoryOptionMapping.ofSku(racket, weight),
                CategoryOptionMapping.ofSku(racket, gripSize),
                CategoryOptionMapping.ofSku(racket, color),
                CategoryOptionMapping.ofSpec(racket, flex),
                CategoryOptionMapping.ofSpec(racket, balance),

                // 셔틀콕: 타입 → SPEC / 속도 → SKU
                CategoryOptionMapping.ofSpec(shuttle, shuttleType),
                CategoryOptionMapping.ofSku(shuttle, shuttleSpeed),

                // 신발: 신발 사이즈·색상 → SKU
                CategoryOptionMapping.ofSku(shoes, shoeSize),
                CategoryOptionMapping.ofSku(shoes, color),

                // 의류: 의류 사이즈·색상 → SKU
                CategoryOptionMapping.ofSku(clothing, clothingSize),
                CategoryOptionMapping.ofSku(clothing, color),

                // 가방: 가방 타입·색상 → SKU
                CategoryOptionMapping.ofSku(bag, bagSize),
                CategoryOptionMapping.ofSku(bag, color),

                // 스트링: 스트링 텐션·색상 → SKU
                CategoryOptionMapping.ofSku(string, color),

                // 그립: 색상 → SKU
                CategoryOptionMapping.ofSku(grip, color),

                // 보호대: 의류 사이즈·색상 → SKU
                CategoryOptionMapping.ofSku(guard, clothingSize),
                CategoryOptionMapping.ofSku(guard, color)
        ));

        log.info("[DataInitializer] 배드민턴 용품 마스터 데이터 삽입 완료.");
    }
}
