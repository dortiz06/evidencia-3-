package com.utch.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;

@JsonPropertyOrder({
        "product_id",
        "title",
        "price_usd",
        "price_mxn",
        "stock_status",
        "available_units",
        "currency_conversion_source",
        "status"
})
public record ProductSummaryResponse(
        @JsonProperty("product_id") String productId,
        String title,
        @JsonProperty("price_usd") BigDecimal priceUsd,
        @JsonProperty("price_mxn") BigDecimal priceMxn,
        @JsonProperty("stock_status") String stockStatus,
        @JsonProperty("available_units") Integer availableUnits,
        @JsonProperty("currency_conversion_source") String currencyConversionSource,
        String status
) {
}
