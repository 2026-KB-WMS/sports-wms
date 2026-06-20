package com.example.sportswms.domain.outbound.service;

import com.example.sportswms.domain.outbound.repository.OutboundDetailRepository;
import com.example.sportswms.domain.outbound.repository.OutboundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OutboundService {
    private final OutboundRepository outboundRepository;
    private final OutboundDetailRepository outboundDetailRepository;


}
