package com.example.sportswms.concurrency.InboundOutbound;

import com.example.sportswms.domain.inbound.entity.Inbound;
import com.example.sportswms.domain.inbound.entity.InboundStatus;
import com.example.sportswms.domain.inbound.repository.InboundRepository;
import com.example.sportswms.domain.order.entity.StockOrder;
import com.example.sportswms.domain.order.entity.Store;
import com.example.sportswms.domain.order.repository.StockOrderRepository;
import com.example.sportswms.domain.order.repository.StoreRepository;
import com.example.sportswms.domain.outbound.entity.Outbound;
import com.example.sportswms.domain.outbound.entity.OutboundStatus;
import com.example.sportswms.domain.outbound.repository.OutboundRepository;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
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

@SpringBootTest
@ActiveProfiles("test")
class InboundOutboundConcurrencyTest {

    @Autowired InboundRepository inboundRepository;
    @Autowired OutboundRepository outboundRepository;
    @Autowired WarehouseRepository warehouseRepository;
    @Autowired StoreRepository storeRepository;
    @Autowired StockOrderRepository stockOrderRepository;
    @Autowired InboundStatusHelper inboundStatusHelper;
    @Autowired OutboundStatusHelper outboundStatusHelper;

    private Long inboundId;
    private Long outboundId;
    private static final int THREAD_COUNT = 100;

    @BeforeEach
    void setUp() {
        Warehouse warehouse = warehouseRepository.save(
                Warehouse.ofDummy("테스트창고", "서울시 강남구", 5000));

        Inbound inbound = inboundRepository.save(Inbound.create(warehouse));
        inboundId = inbound.getId();

        Store store = storeRepository.save(Store.ofDummy("테스트지점", "서울시 강남구", "010-0000-0000"));
        StockOrder stockOrder = stockOrderRepository.save(StockOrder.create(warehouse));
        Outbound outbound = outboundRepository.save(Outbound.create(warehouse, store, stockOrder));
        outboundId = outbound.getId();
    }

    @AfterEach
    void tearDown() {
        outboundRepository.deleteAll();
        stockOrderRepository.deleteAll();
        storeRepository.deleteAll();
        inboundRepository.deleteAll();
        warehouseRepository.deleteAll();
    }

    @Test
    @DisplayName("Inbound 낙관적 락 — 100개 스레드 동시 상태 전이 시 정확히 1개만 성공")
    void concurrentInboundStatusChange_onlyOneSucceeds() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    startGate.await();
                    inboundStatusHelper.advanceStatus(inboundId, InboundStatus.RECEIVED);
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

        Inbound result = inboundRepository.findById(inboundId).orElseThrow();

        System.out.println("=== Inbound 낙관적 락 동시성 테스트 결과 ===");
        System.out.println("스레드 수: " + THREAD_COUNT);
        System.out.println("성공: " + successCount.get() + "건 / 실패(충돌): " + failCount.get() + "건");
        System.out.println("최종 상태: " + result.getStatus());

        assertThat(successCount.get()).as("상태 전이는 정확히 1번만 성공해야 한다").isEqualTo(1);
        assertThat(result.getStatus()).as("최종 상태는 RECEIVED여야 한다").isEqualTo(InboundStatus.RECEIVED);
        System.out.println("✅ Inbound 상태 전이 정합성 보장 확인");
    }

    @Test
    @DisplayName("Outbound 낙관적 락 — 100개 스레드 동시 승인 시 정확히 1개만 성공")
    void concurrentOutboundApprove_onlyOneSucceeds() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    startGate.await();
                    outboundStatusHelper.approve(outboundId);
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

        Outbound result = outboundRepository.findById(outboundId).orElseThrow();

        System.out.println("=== Outbound 낙관적 락 동시성 테스트 결과 ===");
        System.out.println("스레드 수: " + THREAD_COUNT);
        System.out.println("성공: " + successCount.get() + "건 / 실패(충돌): " + failCount.get() + "건");
        System.out.println("최종 상태: " + result.getStatus());

        assertThat(successCount.get()).as("출고 승인은 정확히 1번만 성공해야 한다").isEqualTo(1);
        assertThat(result.getStatus()).as("최종 상태는 APPROVED여야 한다").isEqualTo(OutboundStatus.APPROVED);
        System.out.println("✅ Outbound 상태 전이 정합성 보장 확인");
    }
}