package com.rares.budget_management_app.budget.dto;

import com.rares.budget_management_app.common.Constants;
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
    @NotNull(message = Constants.CATEGORY_ID_REQUIRED)
    private Integer categoryId;

    @NotNull(message = Constants.BUDGET_VALUE_REQUIRED)
    @DecimalMin(value = Constants.MIN_VALUE, message = Constants.BUDGET_MUST_BE_POSITIVE)
    private BigDecimal value;

    private String currency;

    private String month;

    private Integer year;
}
