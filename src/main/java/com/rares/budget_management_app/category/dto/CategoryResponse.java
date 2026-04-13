package com.rares.budget_management_app.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    private Integer id;
    private String name;
    private LocalDateTime createdAt;
    private MonthDetails monthDetails;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MonthDetails {
        private BigDecimal budgetLimit;
        private BigDecimal moneySpent;
        private BigDecimal moneyRemaining;
        private Double percentageSpent;
    }
}
