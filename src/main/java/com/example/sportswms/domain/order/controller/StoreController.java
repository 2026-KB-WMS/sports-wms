package com.example.sportswms.domain.order.controller;

import com.example.sportswms.domain.order.service.StoreService;
import com.example.sportswms.domain.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/store")
public class StoreController {
    private final StoreService storeService;

    @GetMapping
    public String storePage(Model model) {
        return "store";
    }
}
