package com.utch.ecommerce.service;

import com.utch.ecommerce.dto.ProductDto;
import com.utch.ecommerce.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class ProductCatalogService {

    private final Map<String, ProductDto> products = Map.of(
            "P-101", new ProductDto(
                    "P-101",
                    "Laptop Gamer XYZ",
                    "Laptop para gaming de alto rendimiento",
                    "Computadoras",
                    new BigDecimal("1200.00")),
            "P-102", new ProductDto(
                    "P-102",
                    "Mouse Inalambrico Pro",
                    "Mouse ergonómico inalámbrico",
                    "Accesorios",
                    new BigDecimal("49.99")),
            "P-103", new ProductDto(
                    "P-103",
                    "Monitor 27 144Hz",
                    "Monitor IPS de 27 pulgadas",
                    "Monitores",
                    new BigDecimal("329.50"))
    );

    public ProductDto findById(String id) {
        ProductDto product = products.get(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        return product;
    }
}
