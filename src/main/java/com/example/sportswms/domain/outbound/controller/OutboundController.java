package com.example.sportswms.domain.outbound.controller;

import com.example.sportswms.domain.outbound.service.OutboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/outbound")
@RequiredArgsConstructor
public class OutboundController {
    private final OutboundService outboundService;
}
