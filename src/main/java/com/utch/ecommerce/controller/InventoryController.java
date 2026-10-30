package com.utch.ecommerce.controller;

import com.utch.ecommerce.dto.InventoryDto;
import com.utch.ecommerce.service.InventoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/microservices/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public InventoryDto getByProductId(@PathVariable String productId) {
        return inventoryService.findByProductId(productId);
    }
}
