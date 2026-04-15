package com.rares.budget_management_app.common.currency;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrencyRateResponse {

    @JsonProperty("base")
    private String base;

    @JsonProperty("rates")
    private Map<String, BigDecimal> rates;
}