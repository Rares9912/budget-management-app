package com.rares.budget_management_app.expense.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseRequest {

    @NotNull(message = "Id-ul categoriei este obligatoriu")
    private Integer categoryId;

    @NotNull(message = "Valoarea cheltuielii este obligatorie")
    @DecimalMin(value = "0.01", message = "Valoarea cheltuielii trebuie sa fie pozitiva")
    private BigDecimal value;

    private String description;

    private LocalDate expenseDate;

    private String currency;
}
