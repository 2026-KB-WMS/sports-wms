package com.example.sportswms.global.init;

import com.example.sportswms.domain.inventory.entity.Inventory;
import com.example.sportswms.domain.inventory.entity.InventoryTransaction;
import com.example.sportswms.domain.inventory.entity.InventoryStatus;
import com.example.sportswms.domain.inventory.entity.TransactionType;
import com.example.sportswms.domain.inventory.repository.InventoryRepository;
import com.example.sportswms.domain.inventory.repository.InventoryTransactionRepository;
import com.example.sportswms.domain.inbound.entity.Inbound;
import com.example.sportswms.domain.inbound.entity.InboundDetail;
import com.example.sportswms.domain.inbound.entity.InboundStatus;
import com.example.sportswms.domain.inbound.repository.InboundRepository;
import com.example.sportswms.domain.inbound.repository.InboundDetailRepository;
import com.example.sportswms.domain.outbound.entity.Outbound;
import com.example.sportswms.domain.outbound.entity.OutboundDetail;
import com.example.sportswms.domain.outbound.repository.OutboundRepository;
import com.example.sportswms.domain.outbound.repository.OutboundDetailRepository;
import com.example.sportswms.domain.order.entity.*;
import com.example.sportswms.domain.order.repository.*;
import com.example.sportswms.domain.product.entity.*;
import com.example.sportswms.domain.product.repository.*;
import com.example.sportswms.domain.user.entity.Role;
import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.user.entity.UserStatus;
import com.example.sportswms.domain.user.repository.UserRepository;
import com.example.sportswms.domain.warehouse.entity.*;
import com.example.sportswms.domain.warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "wms.init.enabled", havingValue = "true", matchIfMissing = true)
public class DummyDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;
    private final SectionRepository sectionRepository;
    private final WarehouseManagementRepository warehouseManagementRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final ProductSKURepository productSKURepository;
    private final OptionGroupRepository optionGroupRepository;
    private final OptionValueRepository optionValueRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryOptionMappingRepository categoryOptionMappingRepository;
    private final StoreRepository storeRepository;
    private final StoreManagementRepository storeManagementRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final StockOrderRepository stockOrderRepository;
    private final StockOrderDetailRepository stockOrderDetailRepository;
    private final InboundRepository inboundRepository;
    private final InboundDetailRepository inboundDetailRepository;
    private final OutboundRepository outboundRepository;
    private final OutboundDetailRepository outboundDetailRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random(42);

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 1) {
            log.info("[DummyDataInitializer] 더미 데이터가 이미 존재합니다. 스킵합니다.");
            return;
        }

        log.info("[DummyDataInitializer] 더미 데이터 삽입 시작.");

        User admin = createAdmin();
        List<User> warehouseManagers = createWarehouseManagers(5);
        List<User> storeOwners = createStoreOwners(10);

        List<Warehouse> warehouses = createWarehouses(5);
        assignWarehouseManagers(warehouses, warehouseManagers, admin);

        List<Store> stores = createStores(10);
        assignStoreOwners(stores, storeOwners);

        List<Brand> brands = createBrands();
        List<ProductSKU> skus = createProductsAndSkus(brands);

        createInventories(warehouses, skus, admin);
        createOrdersAndInboundsAndOutbounds(warehouses, stores, skus, admin);

        log.info("[DummyDataInitializer] 더미 데이터 삽입 완료. " +
                "창고: {}, SKU: {}, 재고: {}, 입고: {}, 출고: {}",
                warehouses.size(), skus.size(), inventoryRepository.count(),
                inboundRepository.count(), outboundRepository.count());
    }

    // ── 회원 ──────────────────────────────────────────────────────────────────

    private User createAdmin() {
        if (userRepository.findByLoginId("admin").isPresent()) {
            return userRepository.findByLoginId("admin").get();
        }
        return userRepository.save(createUser("admin", "본사관리자", Role.ROLE_GENERAL_MANAGER, "admin@wms.com", "010-0000-0001"));
    }

    private List<User> createWarehouseManagers(int count) {
        List<User> managers = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String loginId = "wm" + i;
            if (userRepository.findByLoginId(loginId).isEmpty()) {
                managers.add(userRepository.save(createUser(loginId, "창고관리자" + i, Role.ROLE_WAREHOUSE_MANAGER,
                        "wm" + i + "@wms.com", "010-1000-" + String.format("%04d", i))));
            }
        }
        return managers;
    }

    private List<User> createStoreOwners(int count) {
        List<User> owners = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String loginId = "owner" + i;
            if (userRepository.findByLoginId(loginId).isEmpty()) {
                owners.add(userRepository.save(createUser(loginId, "점주" + i, Role.ROLE_USER,
                        "owner" + i + "@wms.com", "010-2000-" + String.format("%04d", i))));
            }
        }
        return owners;
    }

    private User createUser(String loginId, String name, Role role, String email, String phone) {
        return User.ofDummy(loginId, passwordEncoder.encode("Test1234!"), role, email, name, phone, "서울시 강남구", UserStatus.APPROVED);
    }

    // ── 창고 / 구역 ────────────────────────────────────────────────────────────

    private List<Warehouse> createWarehouses(int count) {
        String[] names = {"서울 동부 물류센터", "경기 북부 물류센터", "인천 항만 물류센터", "부산 남부 물류센터", "대전 중부 물류센터"};
        String[] addresses = {"서울시 송파구 올림픽로 300", "경기도 의정부시 호국로 1000", "인천시 중구 항동 200", "부산시 사하구 낙동대로 500", "대전시 유성구 대학로 300"};

        List<Warehouse> warehouses = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Warehouse w = Warehouse.ofDummy(names[i], addresses[i], 10000);
            warehouseRepository.save(w);

            // 구역 생성: 각 창고당 보관구역 3개 + 불량구역 1개 + 입출고 완충구역 1개
            createSections(w);
            warehouses.add(w);
        }
        return warehouses;
    }

    private void createSections(Warehouse warehouse) {
        String wCode = "W" + warehouse.getId();

        for (int i = 1; i <= 3; i++) {
            SectionType type = i <= 2 ? SectionType.RACKET_ZONE : SectionType.APPAREL_SHOES_ZONE;
            String code = wCode + "-" + type.getCode() + "-" + String.format("%02d", i);
            Section s = Section.ofDummy(warehouse, type.name() + "-" + i, 1500, type, code);
            warehouse.addSectionCapacity(1500);
            sectionRepository.save(s);
        }

        // STAGING_ZONE
        Section staging = Section.ofDummy(warehouse, "입출고 완충구역", 500, SectionType.STAGING_ZONE,
                wCode + "-" + SectionType.STAGING_ZONE.getCode() + "-01");
        warehouse.addSectionCapacity(500);
        sectionRepository.save(staging);

        // DAMAGED_ZONE
        Section damaged = Section.ofDummy(warehouse, "불량품 보관구역", 300, SectionType.DAMAGED_ZONE,
                wCode + "-" + SectionType.DAMAGED_ZONE.getCode() + "-01");
        warehouse.addSectionCapacity(300);
        sectionRepository.save(damaged);
    }

    private void assignWarehouseManagers(List<Warehouse> warehouses, List<User> managers, User admin) {
        for (int i = 0; i < warehouses.size() && i < managers.size(); i++) {
            WarehouseManagement wm = WarehouseManagement.of(warehouses.get(i), managers.get(i), WarehouseManagementType.MASTER);
            warehouseManagementRepository.save(wm);
        }
    }

    // ── 지점 ──────────────────────────────────────────────────────────────────

    private List<Store> createStores(int count) {
        String[] names = {"강남점", "홍대점", "신촌점", "잠실점", "건대점", "인천점", "수원점", "부산점", "대전점", "광주점"};
        String[] addresses = {"서울시 강남구 테헤란로 1", "서울시 마포구 홍대로 1", "서울시 서대문구 신촌로 1",
                "서울시 송파구 잠실로 1", "서울시 광진구 건대로 1", "인천시 남동구 인주대로 1",
                "경기도 수원시 팔달구 1", "부산시 해운대구 해운대로 1", "대전시 서구 대전로 1", "광주시 북구 광주로 1"};

        List<Store> stores = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Store s = Store.ofDummy(names[i], addresses[i], "010-3000-" + String.format("%04d", i + 1));
            stores.add(storeRepository.save(s));
        }
        return stores;
    }

    private void assignStoreOwners(List<Store> stores, List<User> owners) {
        for (int i = 0; i < stores.size() && i < owners.size(); i++) {
            StoreManagement sm = StoreManagement.of(stores.get(i), owners.get(i), StoreManagementType.OWNER);
            storeManagementRepository.save(sm);
        }
    }

    // ── 브랜드 / 상품 / SKU ────────────────────────────────────────────────────

    private List<Brand> createBrands() {
        List<Brand> brands = new ArrayList<>();
        String[][] data = {
                {"Yonex", "YNX"}, {"Victor", "VIC"}, {"Li-Ning", "LIN"},
                {"Babolat", "BAB"}, {"Carlton", "CAR"}
        };
        for (String[] d : data) {
            if (brandRepository.findByCode(d[1]).isEmpty()) {
                brands.add(brandRepository.save(Brand.of(d[0], d[1])));
            }
        }
        return brands;
    }

    private List<ProductSKU> createProductsAndSkus(List<Brand> brands) {
        List<ProductSKU> allSkus = new ArrayList<>();

        Category racketCategory = categoryRepository.findByName("라켓").orElse(null);
        Category shuttleCategory = categoryRepository.findByName("셔틀콕").orElse(null);
        Category shoesCategory = categoryRepository.findByName("신발").orElse(null);
        Category clothingCategory = categoryRepository.findByName("의류").orElse(null);

        if (racketCategory == null) return allSkus;

        // 라켓 옵션값
        List<OptionValue> weights = optionValueRepository.findByOptionGroupName("무게");
        List<OptionValue> grips = optionValueRepository.findByOptionGroupName("그립 사이즈");
        List<OptionValue> colors = optionValueRepository.findByOptionGroupName("색상");

        // 라켓 상품 & SKU (브랜드별 2개 상품, 각 4 SKU)
        String[][] racketNames = {
                {"Astrox 99", "AX99"}, {"Nanoflare 1000", "NF1000"},
                {"Thruster F", "TF"}, {"Jetspeed S12", "JS12"},
                {"N90iii", "N90-3"}, {"Turbo N50-3", "TN50"},
                {"Saturnus", "SAT"}, {"X-Feel Origin Power", "XFOP"},
                {"Air Series", "AIR"}, {"Powersword", "PWS"}
        };

        for (int i = 0; i < racketNames.length; i++) {
            Brand brand = brands.get(i % brands.size());
            String code = "RAC-" + racketNames[i][1];
            if (productRepository.findByCode(code).isPresent()) continue;

            Product product = productRepository.save(
                    Product.ofDummy(racketNames[i][0], code, brand, 150000 + (i * 10000), racketCategory));

            // SKU: 무게 x 그립 조합
            for (OptionValue w : weights.subList(0, 2)) {
                for (OptionValue g : grips.subList(0, 2)) {
                    String skuName = product.getName() + " " + w.getName() + " " + g.getName();
                    String skuCode = code + "-" + w.getCode() + "-" + g.getCode();
                    ProductSKU sku = ProductSKU.of(product, skuName, skuCode);
                    sku.addOptionValues(List.of(w, g));
                    if (!colors.isEmpty()) sku.addOptionValue(colors.get(0));
                    allSkus.add(productSKURepository.save(sku));
                }
            }
        }

        // 셔틀콕 상품 & SKU
        if (shuttleCategory != null) {
            List<OptionValue> speeds = optionValueRepository.findByOptionGroupName("셔틀콕 속도");
            String[][] shuttleNames = {
                    {"AS-9", "AS9"}, {"Mavis 600", "MV600"}, {"AS-30", "AS30"}
            };
            for (int i = 0; i < shuttleNames.length; i++) {
                Brand brand = brands.get(i % brands.size());
                String code = "SHU-" + shuttleNames[i][1];
                if (productRepository.findByCode(code).isPresent()) continue;

                Product product = productRepository.save(
                        Product.ofDummy(shuttleNames[i][0], code, brand, 20000 + (i * 5000), shuttleCategory));

                for (OptionValue speed : speeds.subList(0, 3)) {
                    String skuCode = code + "-" + speed.getCode();
                    ProductSKU sku = ProductSKU.of(product, product.getName() + " " + speed.getName(), skuCode);
                    sku.addOptionValue(speed);
                    allSkus.add(productSKURepository.save(sku));
                }
            }
        }

        // 신발 상품 & SKU
        if (shoesCategory != null) {
            List<OptionValue> shoeSizes = optionValueRepository.findByOptionGroupName("신발 사이즈");
            String[][] shoeNames = {{"Power Cushion 65Z3", "PC65Z3"}, {"Aerus Z2", "AEZ2"}};
            for (int i = 0; i < shoeNames.length; i++) {
                Brand brand = brands.get(i % brands.size());
                String code = "SHO-" + shoeNames[i][1];
                if (productRepository.findByCode(code).isPresent()) continue;

                Product product = productRepository.save(
                        Product.ofDummy(shoeNames[i][0], code, brand, 120000 + (i * 20000), shoesCategory));

                for (OptionValue size : shoeSizes.subList(0, 4)) {
                    String skuCode = code + "-" + size.getCode();
                    ProductSKU sku = ProductSKU.of(product, product.getName() + " " + size.getName(), skuCode);
                    sku.addOptionValues(List.of(size));
                    if (!colors.isEmpty()) sku.addOptionValue(colors.get(0));
                    allSkus.add(productSKURepository.save(sku));
                }
            }
        }

        return allSkus;
    }

    // ── 재고 ──────────────────────────────────────────────────────────────────

    private void createInventories(List<Warehouse> warehouses, List<ProductSKU> skus, User admin) {
        if (skus.isEmpty()) return;

        for (Warehouse warehouse : warehouses) {
            List<Section> sections = sectionRepository.findAllByWarehouse(warehouse).stream()
                    .filter(s -> s.getSectionType() != SectionType.DAMAGED_ZONE
                            && s.getSectionType() != SectionType.STAGING_ZONE)
                    .toList();

            if (sections.isEmpty()) continue;

            // 각 구역에 SKU를 분배하여 재고 생성
            int skuIndex = 0;
            for (Section section : sections) {
                int skusPerSection = Math.min(skus.size() / (warehouses.size() * sections.size()) + 5, skus.size());

                for (int i = 0; i < skusPerSection && skuIndex < skus.size(); i++, skuIndex++) {
                    ProductSKU sku = skus.get(skuIndex % skus.size());

                    // 이미 해당 구역에 해당 SKU 재고가 있으면 스킵
                    if (inventoryRepository.findBySectionAndProductSKU(section, sku).isPresent()) continue;

                    int quantity = 50 + random.nextInt(200);
                    int usageToAdd = Math.min(quantity, section.getRemainingCapacity());
                    if (usageToAdd <= 0) continue;

                    section.increaseUsage(usageToAdd);
                    Inventory inventory = Inventory.create(section, sku, usageToAdd);
                    inventoryRepository.save(inventory);

                    inventoryTransactionRepository.save(InventoryTransaction.of(
                            section, sku, TransactionType.STACKING_COMPLETE,
                            usageToAdd, InventoryStatus.UNALLOCATED,
                            0, usageToAdd,
                            "초기 더미 데이터 입고", admin
                    ));
                }
            }
        }
    }

    // ── 발주 / 입고 / 출고 ───────────────────────────────────────────────────

    private void createOrdersAndInboundsAndOutbounds(
            List<Warehouse> warehouses, List<Store> stores, List<ProductSKU> skus, User admin) {

        if (skus.isEmpty() || stores.isEmpty() || warehouses.isEmpty()) return;

        // 발주 20건 생성
        for (int i = 0; i < 20; i++) {
            Warehouse warehouse = warehouses.get(i % warehouses.size());
            Store store = stores.get(i % stores.size());

            // StockOrder 생성
            StockOrder stockOrder = stockOrderRepository.save(StockOrder.create(warehouse));

            // Outbound 생성
            Outbound outbound = outboundRepository.save(Outbound.create(warehouse, store, stockOrder));

            // StockOrderDetail + OutboundDetail 2~3개 생성
            int detailCount = 2 + (i % 2);
            for (int j = 0; j < detailCount; j++) {
                ProductSKU sku = skus.get((i + j) % skus.size());
                String groupId = "REQ-" + String.format("%08d", i + 1);
                StockOrderDetail orderDetail = stockOrderDetailRepository.save(
                        StockOrderDetail.ofDummy(store, groupId, sku, 5 + random.nextInt(10)));

                // OutboundDetail 생성 (StockOrderDetail과 연결)
                outboundDetailRepository.save(OutboundDetail.from(outbound, orderDetail));
            }
        }

        // 입고 20건 생성
        for (int i = 0; i < 20; i++) {
            Warehouse warehouse = warehouses.get(i % warehouses.size());

            Inbound inbound = inboundRepository.save(Inbound.create(warehouse));

            // InboundDetail 2~3개 생성
            int detailCount = 2 + (i % 2);
            for (int j = 0; j < detailCount; j++) {
                ProductSKU sku = skus.get((i + j) % skus.size());
                inboundDetailRepository.save(InboundDetail.create(inbound, sku, 10 + random.nextInt(20)));
            }
        }

        log.info("[DummyDataInitializer] 발주 20건, 입고 20건, 출고 20건 생성 완료.");
    }
}
