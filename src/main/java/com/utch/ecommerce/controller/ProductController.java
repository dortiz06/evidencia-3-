package com.utch.ecommerce.controller;

import com.utch.ecommerce.dto.ProductDto;
import com.utch.ecommerce.service.ProductCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/microservices/products")
public class ProductController {

    private final ProductCatalogService productCatalogService;

    public ProductController(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @GetMapping("/{id}")
    public ProductDto getById(@PathVariable String id) {
        return productCatalogService.findById(id);
    }
}
