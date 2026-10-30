package com.utch.ecommerce.client;

import com.utch.ecommerce.dto.InventoryDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.inventory.base-url}") String baseUrl) {
        this.restClient = restClientBuilder.clone().baseUrl(baseUrl).build();
    }

    public InventoryDto getInventory(String id) {
        return restClient.get()
                .uri("/microservices/inventory/{id}", id)
                .retrieve()
                .body(InventoryDto.class);
    }
}
