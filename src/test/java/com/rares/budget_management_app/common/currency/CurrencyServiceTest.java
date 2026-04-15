package com.rares.budget_management_app.common.currency;

import com.rares.budget_management_app.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock private RestClient restClient;
    @Mock private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;
    @Mock private RestClient.RequestHeadersSpec<?> requestHeadersSpec;
    @Mock private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(currencyService, "apiUrl", "https://api.frankfurter.app/latest");
    }

    @Test
    void getExchangeRate_returnsOne_whenSameCurrency() {
        BigDecimal rate = currencyService.getExchangeRate("EUR", "EUR");

        assertThat(rate).isEqualByComparingTo(BigDecimal.ONE);
        verifyNoInteractions(restClient);
    }

    @Test
    void getExchangeRate_returnsOne_whenSameCurrencyDifferentCase() {
        BigDecimal rate = currencyService.getExchangeRate("EUR", "eur");

        assertThat(rate).isEqualByComparingTo(BigDecimal.ONE);
        verifyNoInteractions(restClient);
    }

    @Test
    void getExchangeRate_returnsRate_whenApiCallSucceeds() {
        CurrencyRateResponse response = new CurrencyRateResponse();
        response.setBase("USD");
        response.setRates(Map.of("EUR", new BigDecimal("0.920000")));

        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        doReturn(response).when(responseSpec).body(CurrencyRateResponse.class);

        BigDecimal rate = currencyService.getExchangeRate("USD", "EUR");

        assertThat(rate).isEqualByComparingTo("0.920000");
    }

    @Test
    void getExchangeRate_throwsResourceNotFoundException_whenResponseIsNull() {
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        doReturn(null).when(responseSpec).body(CurrencyRateResponse.class);

        assertThatThrownBy(() -> currencyService.getExchangeRate("USD", "EUR"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getExchangeRate_throwsResourceNotFoundException_whenTargetCurrencyNotInRates() {
        CurrencyRateResponse response = new CurrencyRateResponse();
        response.setBase("USD");
        response.setRates(Map.of("RON", new BigDecimal("4.50")));

        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        doReturn(response).when(responseSpec).body(CurrencyRateResponse.class);

        assertThatThrownBy(() -> currencyService.getExchangeRate("USD", "EUR"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getExchangeRate_throwsResourceNotFoundException_whenApiCallFails() {
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        doThrow(new RestClientException("Connection refused"))
                .when(responseSpec).body(CurrencyRateResponse.class);

        assertThatThrownBy(() -> currencyService.getExchangeRate("USD", "EUR"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}