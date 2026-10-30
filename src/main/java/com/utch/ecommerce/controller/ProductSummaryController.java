package com.utch.ecommerce.controller;

import com.utch.ecommerce.dto.ProductSummaryResponse;
import com.utch.ecommerce.service.ProductSummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/product-summary")
public class ProductSummaryController {

    private final ProductSummaryService productSummaryService;

    public ProductSummaryController(ProductSummaryService productSummaryService) {
        this.productSummaryService = productSummaryService;
    }

    @GetMapping("/{id}")
    public ProductSummaryResponse getById(@PathVariable String id) {
        return productSummaryService.getSummary(id);
    }
}
