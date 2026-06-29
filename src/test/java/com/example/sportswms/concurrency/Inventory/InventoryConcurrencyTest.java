package com.example.sportswms.concurrency.Inventory;

import com.example.sportswms.domain.inventory.entity.Inventory;
import com.example.sportswms.domain.inventory.repository.InventoryRepository;
import com.example.sportswms.domain.product.entity.Brand;
import com.example.sportswms.domain.product.entity.Category;
import com.example.sportswms.domain.product.entity.Product;
import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.product.repository.BrandRepository;
import com.example.sportswms.domain.product.repository.CategoryRepository;
import com.example.sportswms.domain.product.repository.ProductRepository;
import com.example.sportswms.domain.product.repository.ProductSKURepository;
import com.example.sportswms.domain.warehouse.entity.Section;
import com.example.sportswms.domain.warehouse.entity.SectionType;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import com.example.sportswms.domain.warehouse.repository.SectionRepository;
import com.example.sportswms.domain.warehouse.repository.WarehouseRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 낙관적 락(@Version) 동작 검증 테스트.
 *
 * 시나리오: 재고 100개에 100개 스레드가 동시에 1개씩 allocate() 시도
 *
 * 기대 결과:
 * - 충돌한 트랜잭션은 OptimisticLockingFailureException으로 실패
 * - 성공한 트랜잭션 수 == 최종 allocatedQuantity (데이터 정합성 보장)
 * - 최종 allocatedQuantity <= actualQuantity (재고 초과 없음)
 */
@SpringBootTest
@ActiveProfiles("test")
class InventoryConcurrencyTest {

    @Autowired InventoryRepository inventoryRepository;
    @Autowired WarehouseRepository warehouseRepository;
    @Autowired SectionRepository sectionRepository;
    @Autowired BrandRepository brandRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired ProductSKURepository productSKURepository;
    @Autowired InventoryAllocateHelper allocateHelper;

    private Long inventoryId;
    private static final int TOTAL_STOCK = 100;
    private static final int THREAD_COUNT = 100;

    @BeforeEach
    void setUp() {
        Warehouse warehouse = warehouseRepository.save(
                Warehouse.ofDummy("테스트창고", "서울시 강남구", 5000));

        Section section = sectionRepository.save(
                Section.ofDummy(warehouse, "테스트구역", 1000, SectionType.RACKET_ZONE, "W99-RAC-01"));
        warehouse.addSectionCapacity(1000);

        Brand brand = brandRepository.save(Brand.of("테스트브랜드", "TST"));
        Category category = categoryRepository.save(Category.of("테스트카테고리"));
        Product product = productRepository.save(
                Product.ofDummy("테스트상품", "TST-001", brand, 10000, category));
        ProductSKU sku = productSKURepository.save(
                ProductSKU.of(product, "테스트SKU", "TST-001-A"));

        section.increaseUsage(TOTAL_STOCK);
        Inventory inventory = inventoryRepository.save(Inventory.create(section, sku, TOTAL_STOCK));
        inventoryId = inventory.getId();
    }

    @AfterEach
    void tearDown() {
        inventoryRepository.deleteAll();
        productSKURepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        brandRepository.deleteAll();
        sectionRepository.deleteAll();
        warehouseRepository.deleteAll();
    }

    @Test
    @DisplayName("낙관적 락 — 100개 스레드 동시 allocate 시 성공 수 == allocatedQuantity (데이터 정합성 보장)")
    void concurrentAllocate_withOptimisticLock_dataIntegrityGuaranteed() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    startGate.await();
                    allocateHelper.allocate(inventoryId, 1);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startGate.countDown();
        endLatch.await();
        executor.shutdown();

        Inventory result = inventoryRepository.findById(inventoryId).orElseThrow();

        System.out.println("=== 낙관적 락 동시성 테스트 결과 ===");
        System.out.println("스레드 수: " + THREAD_COUNT + " / 실제 재고: " + TOTAL_STOCK);
        System.out.println("성공: " + successCount.get() + "건 / 실패(충돌): " + failCount.get() + "건");
        System.out.println("최종 allocatedQuantity: " + result.getAllocatedQuantity());

        // 핵심 검증 1: 성공 수 == allocatedQuantity (Lost Update 없음)
        assertThat(result.getAllocatedQuantity())
                .as("성공한 트랜잭션 수만큼 정확히 할당돼야 한다 (Lost Update 없음)")
                .isEqualTo(successCount.get());

        // 핵심 검증 2: allocatedQuantity <= actualQuantity (재고 초과 없음)
        assertThat(result.getAllocatedQuantity())
                .as("할당 수량은 실제 재고를 초과할 수 없다")
                .isLessThanOrEqualTo(result.getActualQuantity());

        System.out.println("✅ 데이터 정합성 보장 확인");
        System.out.println("   → 성공(" + successCount.get() + ") == allocatedQuantity("
                + result.getAllocatedQuantity() + ")");
        System.out.println("   → allocatedQuantity(" + result.getAllocatedQuantity()
                + ") <= actualQuantity(" + result.getActualQuantity() + ")");
    }
}
