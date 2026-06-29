package com.example.sportswms.concurrency.InboundOutbound;

import com.example.sportswms.domain.inbound.entity.Inbound;
import com.example.sportswms.domain.inbound.entity.InboundStatus;
import com.example.sportswms.domain.inbound.repository.InboundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InboundStatusHelper {

    private final InboundRepository inboundRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void advanceStatus(Long inboundId, InboundStatus nextStatus) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new IllegalArgumentException("입고를 찾을 수 없습니다."));
        inbound.advanceStatus(nextStatus);
    }
}
