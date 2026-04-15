package com.rares.budget_management_app.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetRequest {
    @NotNull(message = "Id-ul categoriei este obligatoriu")
    private Integer categoryId;

    @NotNull(message = "Valoarea bugetului este obligatorie")
    @DecimalMin(value = "0.01", message = "Valoarea bugetului trebuie sa fie pozitiva")
    private BigDecimal value;

    private String currency;

    private String month;

    private Integer year;
}
