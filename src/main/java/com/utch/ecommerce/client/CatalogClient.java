package com.utch.ecommerce.client;

import com.utch.ecommerce.dto.ProductDto;
import com.utch.ecommerce.exception.ExternalServiceException;
import com.utch.ecommerce.exception.ProductNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.catalog.base-url}") String baseUrl) {
        this.restClient = restClientBuilder.clone().baseUrl(baseUrl).build();
    }

    public ProductDto getProduct(String id) {
        try {
            ProductDto product = restClient.get()
                    .uri("/microservices/products/{id}", id)
                    .retrieve()
                    .body(ProductDto.class);
            if (product == null) {
                throw new ExternalServiceException("Respuesta vacía del servicio de catálogo");
            }
            return product;
        } catch (ProductNotFoundException | ExternalServiceException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                throw new ProductNotFoundException(id);
            }
            throw new ExternalServiceException("El servicio de catálogo no responde", ex);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("El servicio de catálogo no responde", ex);
        }
    }
}
