package com.rares.budget_management_app.common.currency;

import com.rares.budget_management_app.common.exception.Error;
import com.rares.budget_management_app.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final RestClient restClient;

    @Value("${currency.api.url}")
    private String apiUrl;

    @Cacheable(value = "exchangeRates", key = "#fromCurrency + '_' + #toCurrency")
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return BigDecimal.ONE;
        }

        String url = apiUrl + "?from=" + fromCurrency + "&to=" + toCurrency;
        try {
            CurrencyRateResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(CurrencyRateResponse.class);

            if (response == null || response.getRates() == null) {
                throw new ResourceNotFoundException(Error.CURRENCY_CONVERSION_FAILED, fromCurrency);
            }

            BigDecimal rate = response.getRates().get(toCurrency);
            if (rate == null) {
                throw new ResourceNotFoundException(Error.CURRENCY_CONVERSION_FAILED, toCurrency);
            }

            return rate;

        } catch (RestClientException e) {
            throw new ResourceNotFoundException(Error.CURRENCY_CONVERSION_FAILED, fromCurrency);
        }
    }
}