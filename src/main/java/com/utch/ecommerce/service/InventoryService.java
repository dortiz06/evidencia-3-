package com.utch.ecommerce.service;

import com.utch.ecommerce.dto.InventoryDto;
import com.utch.ecommerce.exception.InventoryNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class InventoryService {

    private final Map<String, InventoryDto> inventory = Map.of(
            "P-101", new InventoryDto("P-101", 15, "Almacén Chihuahua", 2),
            "P-102", new InventoryDto("P-102", 0, "Almacén Chihuahua", 2),
            "P-103", new InventoryDto("P-103", 3, "Almacén Chihuahua", 2)
    );

    public InventoryDto findByProductId(String productId) {
        InventoryDto item = inventory.get(productId);
        if (item == null) {
            throw new InventoryNotFoundException(productId);
        }
        return item;
    }
}
