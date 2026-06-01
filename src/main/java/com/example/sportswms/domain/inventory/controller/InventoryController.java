package com.example.sportswms.domain.inventory.controller;

import com.example.sportswms.domain.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping("/inventory")
    public String inventoryPage(Model model) {
        model.addAttribute("inventories", inventoryService.getAllInventories());
        model.addAttribute("transactions", inventoryService.getAllTransactions());
        return "inventory";
    }
}
