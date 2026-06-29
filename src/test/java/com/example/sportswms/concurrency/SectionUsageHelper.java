package com.example.sportswms.concurrency;

import com.example.sportswms.domain.warehouse.entity.Section;
import com.example.sportswms.domain.warehouse.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Section 동시성 테스트에서 각 스레드가 독립적인 트랜잭션으로 동작하도록 분리.
 */
@Component
@RequiredArgsConstructor
public class SectionUsageHelper {

    private final SectionRepository sectionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increaseUsage(Long sectionId, int quantity) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("구역을 찾을 수 없습니다."));
        section.increaseUsage(quantity);
    }
}
