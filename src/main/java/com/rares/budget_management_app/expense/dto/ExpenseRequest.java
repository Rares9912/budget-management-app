package com.rares.budget_management_app.expense.dto;

import com.rares.budget_management_app.common.Constants;
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

    @NotNull(message = Constants.CATEGORY_ID_REQUIRED)
    private Integer categoryId;

    @NotNull(message = Constants.EXPENSE_VALUE_REQUIRED)
    @DecimalMin(value = Constants.MIN_VALUE, message = Constants.EXPENSE_MUST_BE_POSITIVE)
    private BigDecimal value;

    private String description;

    private LocalDate expenseDate;

    private String currency;
}
