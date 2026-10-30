package com.utch.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ProductDto(
        String id,
        String name,
        String description,
        String category,
        @JsonProperty("price_usd") BigDecimal priceUsd
) {
}
