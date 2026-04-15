package com.rares.budget_management_app.admin;

import com.rares.budget_management_app.admin.dto.AdminCreateUserRequest;
import com.rares.budget_management_app.admin.dto.AdminUpdateUserRequest;
import com.rares.budget_management_app.budget.dto.BudgetRequest;
import com.rares.budget_management_app.budget.dto.BudgetResponse;
import com.rares.budget_management_app.category.dto.CategoryRequest;
import com.rares.budget_management_app.category.dto.CategoryResponse;
import com.rares.budget_management_app.expense.dto.ExpenseRequest;
import com.rares.budget_management_app.expense.dto.ExpenseResponse;
import com.rares.budget_management_app.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Month;
import java.util.List;

@Tag(name = "Admin")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(request));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(adminService.getUser(userId));
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Integer userId,
                                                   @Valid @RequestBody AdminUpdateUserRequest request) {
        return ResponseEntity.ok(adminService.updateUser(userId, request));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok("User with id " + userId + " was successfully deleted!");
    }

    @GetMapping("/users/{userId}/categories")
    public ResponseEntity<List<CategoryResponse>> getUserCategories(@PathVariable Integer userId) {
        return ResponseEntity.ok(adminService.getUserCategories(userId));
    }

    @PostMapping("/users/{userId}/categories")
    public ResponseEntity<CategoryResponse> createCategoryForUser(@PathVariable Integer userId,
                                                                   @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(adminService.createCategoryForUser(userId, request.getName()));
    }

    @DeleteMapping("/users/{userId}/categories/{categoryId}")
    public ResponseEntity<String> deleteCategoryForUser(@PathVariable Integer userId,
                                                         @PathVariable Integer categoryId) {
        String deletedName = adminService.deleteCategoryForUser(userId, categoryId);
        return ResponseEntity.ok("Category " + deletedName + " was successfully deleted!");
    }

    @GetMapping("/users/{userId}/budgets")
    public ResponseEntity<List<BudgetResponse>> getUserBudgets(@PathVariable Integer userId,
                                                                @RequestParam(required = false) String category,
                                                                @RequestParam(required = false) String month,
                                                                @RequestParam(required = false) Integer year) {
        Integer monthValue = month != null ? Month.valueOf(month.toUpperCase()).getValue() : null;
        return ResponseEntity.ok(adminService.getUserBudgets(userId, category, monthValue, year));
    }

    @PostMapping("/users/{userId}/budgets")
    public ResponseEntity<BudgetResponse> createBudgetForUser(@PathVariable Integer userId,
                                                               @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(adminService.createBudgetForUser(userId, request));
    }

    @PutMapping("/users/{userId}/budgets")
    public ResponseEntity<BudgetResponse> updateBudgetForUser(@PathVariable Integer userId,
                                                               @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(adminService.updateBudgetForUser(userId, request));
    }

    @GetMapping("/users/{userId}/expenses")
    public ResponseEntity<List<ExpenseResponse>> getUserExpenses(@PathVariable Integer userId,
                                                                  @RequestParam(required = false) String category,
                                                                  @RequestParam(required = false) String month,
                                                                  @RequestParam(required = false) Integer year) {
        Integer monthValue = month != null ? Month.valueOf(month.toUpperCase()).getValue() : null;
        return ResponseEntity.ok(adminService.getUserExpenses(userId, category, monthValue, year));
    }

    @PostMapping("/users/{userId}/expenses")
    public ResponseEntity<ExpenseResponse> createExpenseForUser(@PathVariable Integer userId,
                                                                 @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(adminService.createExpenseForUser(userId, request));
    }

    @PutMapping("/users/{userId}/expenses/{expenseId}")
    public ResponseEntity<ExpenseResponse> updateExpenseForUser(@PathVariable Integer userId,
                                                                 @PathVariable Integer expenseId,
                                                                 @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(adminService.updateExpenseForUser(userId, expenseId, request));
    }

    @DeleteMapping("/users/{userId}/expenses/{expenseId}")
    public ResponseEntity<String> deleteExpenseForUser(@PathVariable Integer userId,
                                                        @PathVariable Integer expenseId) {
        adminService.deleteExpenseForUser(userId, expenseId);
        return ResponseEntity.ok("Expense with id " + expenseId + " was successfully deleted!");
    }
}