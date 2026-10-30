package com.utch.ecommerce.service;

import com.utch.ecommerce.client.CatalogClient;
import com.utch.ecommerce.client.ExchangeRateClient;
import com.utch.ecommerce.client.InventoryClient;
import com.utch.ecommerce.dto.InventoryDto;
import com.utch.ecommerce.dto.ProductDto;
import com.utch.ecommerce.dto.ProductSummaryResponse;
import com.utch.ecommerce.exception.ExternalServiceException;
import com.utch.ecommerce.exception.ProductNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class ProductSummaryService {

    private static final Logger log = LoggerFactory.getLogger(ProductSummaryService.class);
    private static final String CURRENCY_CONVERSION_SOURCE = "Frankfurter External API";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String PARTIAL_SUCCESS_STATUS = "PARTIAL_SUCCESS";
    private static final long TIMEOUT_SECONDS = 5;

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
        CompletableFuture<ProductDto> productFuture = CompletableFuture
                .supplyAsync(() -> catalogClient.getProduct(id), productSummaryExecutor)
                .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .handle((product, error) -> {
                    if (error == null) {
                        return product;
                    }
                    Throwable cause = unwrap(error);
                    log.warn("Fallo en el servicio de catálogo", cause);
                    throw catalogFailure(id, cause);
                });

        CompletableFuture<InventoryDto> inventoryFuture = CompletableFuture
                .supplyAsync(() -> inventoryClient.getInventory(id), productSummaryExecutor)
                .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .exceptionally(error -> {
                    log.warn("Fallo en el servicio de inventario", unwrap(error));
                    return null;
                });

        CompletableFuture<BigDecimal> rateFuture = CompletableFuture
                .supplyAsync(exchangeRateClient::getUsdToMxnRate, productSummaryExecutor)
                .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .exceptionally(error -> {
                    log.warn("Fallo en el servicio de divisas (Frankfurter)", unwrap(error));
                    return null;
                });

        try {
            CompletableFuture.allOf(productFuture, inventoryFuture, rateFuture).join();
        } catch (CompletionException ex) {
            Throwable cause = unwrap(ex);
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new ExternalServiceException("El servicio de catálogo no responde", cause);
        }

        ProductDto product = productFuture.join();
        InventoryDto inventory = inventoryFuture.join();
        BigDecimal usdToMxn = rateFuture.join();

        boolean inventoryFailed = inventory == null;
        boolean fxFailed = usdToMxn == null;

        BigDecimal priceMxn = null;
        String conversionSource = CURRENCY_CONVERSION_SOURCE;
        if (!fxFailed) {
            priceMxn = product.priceUsd().multiply(usdToMxn).setScale(2, RoundingMode.HALF_UP);
        } else {
            conversionSource = "UNAVAILABLE";
        }

        String stockStatus;
        Integer availableUnits;
        if (inventoryFailed) {
            stockStatus = "UNKNOWN";
            availableUnits = null;
        } else {
            availableUnits = inventory.availableUnits();
            stockStatus = stockStatus(availableUnits);
        }

        String status = (inventoryFailed || fxFailed) ? PARTIAL_SUCCESS_STATUS : SUCCESS_STATUS;

        return new ProductSummaryResponse(
                product.id(),
                product.name(),
                product.priceUsd(),
                priceMxn,
                stockStatus,
                availableUnits,
                conversionSource,
                status
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

    private RuntimeException catalogFailure(String id, Throwable cause) {
        if (cause instanceof ProductNotFoundException productNotFound) {
            return productNotFound;
        }
        if (cause instanceof RestClientResponseException responseException
                && responseException.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
            return new ProductNotFoundException(id);
        }
        if (cause instanceof ExternalServiceException externalServiceException) {
            return externalServiceException;
        }
        return new ExternalServiceException("El servicio de catálogo no responde", cause);
    }

    private Throwable unwrap(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null
                && (current instanceof CompletionException || current instanceof java.util.concurrent.ExecutionException)) {
            current = current.getCause();
        }
        return current;
    }
}
