package com.example.sportswms.concurrency;

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
 * Section 낙관적 락(@Version) 동작 검증 테스트.
 *
 * 시나리오: 100개 스레드가 동시에 같은 구역에 1씩 increaseUsage() 시도
 *
 * 기대 결과:
 * - 충돌한 트랜잭션은 OptimisticLockingFailureException으로 실패
 * - 성공한 트랜잭션 수 == 최종 currentUsage (데이터 정합성 보장)
 * - 최종 currentUsage <= totalCapacity (용량 초과 없음)
 */
@SpringBootTest
@ActiveProfiles("test")
class SectionConcurrencyTest {

    @Autowired SectionRepository sectionRepository;
    @Autowired WarehouseRepository warehouseRepository;
    @Autowired SectionUsageHelper sectionUsageHelper;

    private Long sectionId;
    private static final int TOTAL_CAPACITY = 1000;
    private static final int THREAD_COUNT = 100;

    @BeforeEach
    void setUp() {
        Warehouse warehouse = warehouseRepository.save(
                Warehouse.ofDummy("테스트창고", "서울시 강남구", 5000));
        warehouse.addSectionCapacity(TOTAL_CAPACITY);

        Section section = sectionRepository.save(
                Section.ofDummy(warehouse, "테스트구역", TOTAL_CAPACITY, SectionType.RACKET_ZONE, "W99-RAC-01"));
        sectionId = section.getId();
    }

    @AfterEach
    void tearDown() {
        sectionRepository.deleteAll();
        warehouseRepository.deleteAll();
    }

    @Test
    @DisplayName("낙관적 락 — 100개 스레드 동시 increaseUsage 시 성공 수 == currentUsage (데이터 정합성 보장)")
    void concurrentIncreaseUsage_withOptimisticLock_dataIntegrityGuaranteed() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    startGate.await();
                    sectionUsageHelper.increaseUsage(sectionId, 1);
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

        Section result = sectionRepository.findById(sectionId).orElseThrow();

        System.out.println("=== Section 낙관적 락 동시성 테스트 결과 ===");
        System.out.println("스레드 수: " + THREAD_COUNT + " / 총 수용량: " + TOTAL_CAPACITY);
        System.out.println("성공: " + successCount.get() + "건 / 실패(충돌): " + failCount.get() + "건");
        System.out.println("최종 currentUsage: " + result.getCurrentUsage());

        // 핵심 검증 1: 성공 수 == currentUsage (Lost Update 없음)
        assertThat(result.getCurrentUsage())
                .as("성공한 트랜잭션 수만큼 정확히 반영돼야 한다 (Lost Update 없음)")
                .isEqualTo(successCount.get());

        // 핵심 검증 2: currentUsage <= totalCapacity (용량 초과 없음)
        assertThat(result.getCurrentUsage())
                .as("사용량은 총 수용량을 초과할 수 없다")
                .isLessThanOrEqualTo(result.getTotalCapacity());

        System.out.println("✅ 데이터 정합성 보장 확인");
        System.out.println("   → 성공(" + successCount.get() + ") == currentUsage(" + result.getCurrentUsage() + ")");
        System.out.println("   → currentUsage(" + result.getCurrentUsage()
                + ") <= totalCapacity(" + result.getTotalCapacity() + ")");
    }
}
