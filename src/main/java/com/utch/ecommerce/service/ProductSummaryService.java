package com.utch.ecommerce.service;

import com.utch.ecommerce.client.CatalogClient;
import com.utch.ecommerce.client.ExchangeRateClient;
import com.utch.ecommerce.client.InventoryClient;
import com.utch.ecommerce.dto.InventoryDto;
import com.utch.ecommerce.dto.ProductDto;
import com.utch.ecommerce.dto.ProductSummaryResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
public class ProductSummaryService {

    private static final String CURRENCY_CONVERSION_SOURCE = "Frankfurter External API";
    private static final String SUCCESS_STATUS = "SUCCESS";

    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;
    private final ExchangeRateClient exchangeRateClient;
    private final ExecutorService productSummaryExecutor;

    public ProductSummaryService(
            CatalogClient catalogClient,
            InventoryClient inventoryClient,
            ExchangeRateClient exchangeRateClient,
            ExecutorService productSummaryExecutor) {
        this.catalogClient = catalogClient;
        this.inventoryClient = inventoryClient;
        this.exchangeRateClient = exchangeRateClient;
        this.productSummaryExecutor = productSummaryExecutor;
    }

    public ProductSummaryResponse getSummary(String id) {
        CompletableFuture<ProductDto> productFuture = CompletableFuture.supplyAsync(
                () -> catalogClient.getProduct(id), productSummaryExecutor);
        CompletableFuture<InventoryDto> inventoryFuture = CompletableFuture.supplyAsync(
                () -> inventoryClient.getInventory(id), productSummaryExecutor);
        CompletableFuture<BigDecimal> rateFuture = CompletableFuture.supplyAsync(
                exchangeRateClient::getUsdToMxnRate, productSummaryExecutor);

        CompletableFuture.allOf(productFuture, inventoryFuture, rateFuture).join();

        ProductDto product = productFuture.join();
        InventoryDto inventory = inventoryFuture.join();
        BigDecimal usdToMxn = rateFuture.join();

        BigDecimal priceMxn = product.priceUsd()
                .multiply(usdToMxn)
                .setScale(2, RoundingMode.HALF_UP);

        return new ProductSummaryResponse(
                product.id(),
                product.name(),
                product.priceUsd(),
                priceMxn,
                stockStatus(inventory.availableUnits()),
                inventory.availableUnits(),
                CURRENCY_CONVERSION_SOURCE,
                SUCCESS_STATUS
        );
    }

    private String stockStatus(int availableUnits) {
        if (availableUnits > 5) {
            return "IN_STOCK";
        }
        if (availableUnits >= 1) {
            return "LOW_STOCK";
        }
        return "OUT_OF_STOCK";
    }
}
