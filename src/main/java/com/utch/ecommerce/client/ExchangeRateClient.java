package com.utch.ecommerce.client;

import com.utch.ecommerce.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

@Component
public class ExchangeRateClient {

    private final RestClient restClient;

    public ExchangeRateClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.fx.base-url}") String baseUrl) {
        this.restClient = restClientBuilder.clone().baseUrl(baseUrl).build();
    }

    public BigDecimal getUsdToMxnRate() {
        try {
            FrankfurterResponse response = restClient.get()
                    .uri("/latest?from=USD&to=MXN")
                    .retrieve()
                    .body(FrankfurterResponse.class);

            if (response == null || response.rates() == null || response.rates().get("MXN") == null) {
                throw new ExternalServiceException(
                        "Respuesta vacía o sin tasa MXN al consultar el tipo de cambio USD→MXN");
            }
            return response.rates().get("MXN");
        } catch (ExternalServiceException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new ExternalServiceException(
                    "Fallo al consultar el tipo de cambio USD→MXN en Frankfurter", ex);
        }
    }
}
