package com.utch.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InventoryDto(
        @JsonProperty("product_id") String productId,
        @JsonProperty("available_units") int availableUnits,
        String warehouse,
        @JsonProperty("estimated_delivery_days") int estimatedDeliveryDays
) {
}
