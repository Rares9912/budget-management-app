package com.rares.budget_management_app.budget;

import com.rares.budget_management_app.budget.dto.BudgetRequest;
import com.rares.budget_management_app.budget.dto.BudgetResponse;
import com.rares.budget_management_app.common.exception.Error;
import com.rares.budget_management_app.common.exception.InvalidMonthException;
import com.rares.budget_management_app.user.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Month;
import java.util.List;

@Tag(name = "Budget")
@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getBudgets(@AuthenticationPrincipal User user,
                                                           @RequestParam(required = false) String category,
                                                           @RequestParam(required = false) String month,
                                                           @RequestParam(required = false) Integer year) {

        int monthValue;
        try {
            monthValue = Month.valueOf(month.toUpperCase()).getValue();
        } catch (IllegalArgumentException e) {
            throw new InvalidMonthException(Error.INVALID_MONTH, month);
        }

        return ResponseEntity.ok(
                budgetService.getBudgets(user, category, monthValue, year));
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BudgetRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(budgetService.createBudget(user, request));
    }

    @PutMapping
    public ResponseEntity<BudgetResponse> updateBudget(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BudgetRequest request) {

        return ResponseEntity.ok(budgetService.updateBudget(user, request));
    }
}
