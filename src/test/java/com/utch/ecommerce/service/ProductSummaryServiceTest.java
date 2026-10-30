package com.utch.ecommerce.service;

import com.utch.ecommerce.client.CatalogClient;
import com.utch.ecommerce.client.ExchangeRateClient;
import com.utch.ecommerce.client.InventoryClient;
import com.utch.ecommerce.dto.InventoryDto;
import com.utch.ecommerce.dto.ProductDto;
import com.utch.ecommerce.dto.ProductSummaryResponse;
import com.utch.ecommerce.exception.ProductNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSummaryServiceTest {

    private static final String PRODUCT_ID = "P-101";

    @Mock
    private CatalogClient catalogClient;
    @Mock
    private InventoryClient inventoryClient;
    @Mock
    private ExchangeRateClient exchangeRateClient;

    private ExecutorService executor;
    private ProductSummaryService productSummaryService;

    @BeforeEach
    void setUp() {
        executor = Executors.newFixedThreadPool(4);
        productSummaryService = new ProductSummaryService(
                catalogClient, inventoryClient, exchangeRateClient, executor);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void inventoryFailureReturnsPartialSuccessWithUnknownStock() {
        when(catalogClient.getProduct(PRODUCT_ID)).thenReturn(product());
        when(inventoryClient.getInventory(PRODUCT_ID)).thenThrow(new RuntimeException("Inventario caído"));
        when(exchangeRateClient.getUsdToMxnRate()).thenReturn(new BigDecimal("18.75"));

        ProductSummaryResponse response = productSummaryService.getSummary(PRODUCT_ID);

        assertThat(response.status()).isEqualTo("PARTIAL_SUCCESS");
        assertThat(response.stockStatus()).isEqualTo("UNKNOWN");
        assertThat(response.availableUnits()).isNull();
        assertThat(response.priceMxn()).isEqualByComparingTo("22500.00");
        assertThat(response.currencyConversionSource()).isEqualTo("Frankfurter External API");
        assertThat(response.title()).isEqualTo("Laptop Gamer XYZ");
    }

    @Test
    void fxFailureReturnsPartialSuccessWithoutMxnPrice() {
        when(catalogClient.getProduct(PRODUCT_ID)).thenReturn(product());
        when(inventoryClient.getInventory(PRODUCT_ID)).thenReturn(inventory(15));
        when(exchangeRateClient.getUsdToMxnRate()).thenThrow(new RuntimeException("Frankfurter caído"));

        ProductSummaryResponse response = productSummaryService.getSummary(PRODUCT_ID);

        assertThat(response.status()).isEqualTo("PARTIAL_SUCCESS");
        assertThat(response.priceMxn()).isNull();
        assertThat(response.currencyConversionSource()).isEqualTo("UNAVAILABLE");
        assertThat(response.stockStatus()).isEqualTo("IN_STOCK");
        assertThat(response.availableUnits()).isEqualTo(15);
    }

    @Test
    void missingProductThrowsProductNotFoundException() {
        when(catalogClient.getProduct("MISSING")).thenThrow(new ProductNotFoundException("MISSING"));
        when(inventoryClient.getInventory("MISSING")).thenReturn(inventory(15));
        when(exchangeRateClient.getUsdToMxnRate()).thenReturn(new BigDecimal("18.75"));

        assertThatThrownBy(() -> productSummaryService.getSummary("MISSING"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("MISSING");
    }

    private ProductDto product() {
        return new ProductDto(
                PRODUCT_ID,
                "Laptop Gamer XYZ",
                "Laptop para gaming de alto rendimiento",
                "Computadoras",
                new BigDecimal("1200.00"));
    }

    private InventoryDto inventory(int units) {
        return new InventoryDto(PRODUCT_ID, units, "Almacén Chihuahua", 2);
    }
}
