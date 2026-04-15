package com.rares.budget_management_app.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    private Integer id;
    private Integer categoryId;
    private String categoryName;
    private BigDecimal value;
    private String currency;
    private BigDecimal exchangeRate;
    private String description;
    private String budgetWarning;
    private LocalDate expenseDate;
    private LocalDateTime createdAt;
}