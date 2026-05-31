package com.example.sportswms.domain.inventory.controller;

import com.example.sportswms.domain.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;
}
