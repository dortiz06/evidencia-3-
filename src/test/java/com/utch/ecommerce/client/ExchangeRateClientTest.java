package com.utch.ecommerce.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ExchangeRateClientTest {

    private static final String BASE_URL = "https://api.frankfurter.app";

    private ExchangeRateClient exchangeRateClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        exchangeRateClient = new ExchangeRateClient(builder, BASE_URL);
    }

    @Test
    void getUsdToMxnRateExtractsMxnFromResponse() {
        mockServer.expect(requestTo(BASE_URL + "/latest?from=USD&to=MXN"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {"amount":1.0,"base":"USD","date":"2026-10-29","rates":{"MXN":18.75}}
                        """,
                        MediaType.APPLICATION_JSON));

        BigDecimal rate = exchangeRateClient.getUsdToMxnRate();

        assertThat(rate).isEqualByComparingTo(new BigDecimal("18.75"));
        mockServer.verify();
    }
}
