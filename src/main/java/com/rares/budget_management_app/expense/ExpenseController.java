package com.rares.budget_management_app.expense;

import com.rares.budget_management_app.common.exception.Error;
import com.rares.budget_management_app.common.exception.InvalidMonthException;
import com.rares.budget_management_app.expense.dto.ExpenseRequest;
import com.rares.budget_management_app.expense.dto.ExpenseResponse;
import com.rares.budget_management_app.user.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

@Tag(name = "Expenses")
@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getExpenses(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) LocalDate date) {

        int monthValue;
        try {
            monthValue = Month.valueOf(month.toUpperCase()).getValue();
        } catch (IllegalArgumentException e) {
            throw new InvalidMonthException(Error.INVALID_MONTH, month);
        }

        return ResponseEntity.ok(
                expenseService.getExpenses(user, categoryName, monthValue, year, date));
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ExpenseRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(expenseService.createExpense(user, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @AuthenticationPrincipal User user,
            @PathVariable Integer id,
            @Valid @RequestBody ExpenseRequest request) {

        return ResponseEntity.ok(
                expenseService.updateExpense(user, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteExpense(
            @AuthenticationPrincipal User user,
            @PathVariable Integer id) {

        expenseService.deleteExpense(user, id);
        return ResponseEntity.ok("Expense was successfully deleted!");
    }
}
