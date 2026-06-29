package com.example.sportswms.concurrency.InboundOutbound;

import com.example.sportswms.domain.outbound.entity.Outbound;
import com.example.sportswms.domain.outbound.repository.OutboundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboundStatusHelper {

    private final OutboundRepository outboundRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void approve(Long outboundId) {
        Outbound outbound = outboundRepository.findById(outboundId)
                .orElseThrow(() -> new IllegalArgumentException("출고를 찾을 수 없습니다."));
        outbound.approve();
    }
}
