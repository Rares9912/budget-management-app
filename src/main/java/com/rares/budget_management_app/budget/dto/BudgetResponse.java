package com.rares.budget_management_app.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetResponse {
    private Integer id;
    private String categoryName;
    private BigDecimal value;
    private String currency;
    private String month;
    private Integer year;
    private LocalDateTime createdAt;
}
