package com.utch.ecommerce.client;

import com.utch.ecommerce.dto.ProductDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.catalog.base-url}") String baseUrl) {
        this.restClient = restClientBuilder.clone().baseUrl(baseUrl).build();
    }

    public ProductDto getProduct(String id) {
        return restClient.get()
                .uri("/microservices/products/{id}", id)
                .retrieve()
                .body(ProductDto.class);
    }
}
