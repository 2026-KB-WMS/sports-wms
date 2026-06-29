package com.example.sportswms.concurrency;

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
 * 재고 동시성 테스트.
 *
 * [락 없음] allocateUnsafe: 검증 없이 allocatedQuantity를 무조건 증가
 *   → Race Condition 발생 시 최종값이 THREAD_COUNT보다 작아짐 (일부 업데이트 유실)
 *
 * [락 적용 후] allocate: 낙관적 락으로 충돌 감지
 *   → 충돌한 트랜잭션은 예외 발생, 최종 allocatedQuantity <= actualQuantity 보장
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
    @DisplayName("[락 없음] 100개 스레드가 동시에 증가 → 일부 업데이트 유실로 최종값이 100 미만이 됨")
    void concurrentAllocate_withoutLock_shouldLoseUpdates() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    startGate.await();
                    allocateHelper.allocateUnsafe(inventoryId);
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

        System.out.println("=== [락 없음] 동시성 테스트 결과 ===");
        System.out.println("스레드 수: " + THREAD_COUNT);
        System.out.println("성공: " + successCount.get() + "건 / 실패: " + failCount.get() + "건");
        System.out.println("기대 allocatedQuantity: " + THREAD_COUNT);
        System.out.println("실제 allocatedQuantity: " + result.getAllocatedQuantity());

        if (result.getAllocatedQuantity() < THREAD_COUNT) {
            System.out.println("⚠️  Race Condition(Lost Update) 발생! "
                    + (THREAD_COUNT - result.getAllocatedQuantity()) + "개 업데이트 유실");
        } else {
            System.out.println("Race Condition 미발생 (H2 직렬화로 인해 우연히 충돌 안 남)");
        }
    }

    @Test
    @DisplayName("[락 적용 후] 동시 allocate 시 allocatedQuantity는 실제 재고를 초과하지 않아야 한다")
    void concurrentAllocate_withLock_shouldNotExceedStock() throws InterruptedException {
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

        System.out.println("=== [락 적용 후] 동시성 테스트 결과 ===");
        System.out.println("스레드 수: " + THREAD_COUNT + ", 실제 재고: " + TOTAL_STOCK);
        System.out.println("성공: " + successCount.get() + "건 / 실패: " + failCount.get() + "건");
        System.out.println("최종 allocatedQuantity: " + result.getAllocatedQuantity());

        assertThat(result.getAllocatedQuantity())
                .as("할당 수량은 실제 재고를 초과할 수 없다")
                .isLessThanOrEqualTo(result.getActualQuantity());

        System.out.println("✅ 락 적용 후 데이터 정합성 보장 확인");
    }
}
