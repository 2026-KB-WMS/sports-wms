package com.example.sportswms.domain.inventory.controller;

import com.example.sportswms.domain.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/inventory")
    public String inventoryPage(@RequestParam(required = false) Long warehouseId,
                                @RequestParam(required = false) Long sectionId,
                                @RequestParam(required = false) Long skuId,
                                Model model) {
        model.addAttribute("inventories",  inventoryService.getInventories(warehouseId, sectionId, skuId));
        model.addAttribute("transactions", inventoryService.getTransactions(warehouseId, sectionId, skuId));
        model.addAttribute("warehouses",   inventoryService.getAllWarehouses());
        model.addAttribute("sections",     inventoryService.getAllSections());
        model.addAttribute("skus",         inventoryService.getAllSKUs());
        return "inventory";
    }
}
