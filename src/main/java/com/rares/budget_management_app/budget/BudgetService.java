package com.rares.budget_management_app.budget;

import com.rares.budget_management_app.budget.dto.BudgetRequest;
import com.rares.budget_management_app.budget.dto.BudgetResponse;
import com.rares.budget_management_app.category.Category;
import com.rares.budget_management_app.category.CategoryRepository;
import com.rares.budget_management_app.common.currency.CurrencyService;
import com.rares.budget_management_app.common.exception.DuplicateResourceException;
import com.rares.budget_management_app.common.exception.Error;
import com.rares.budget_management_app.common.exception.PastBudgetModificationException;
import com.rares.budget_management_app.common.exception.ResourceNotFoundException;
import com.rares.budget_management_app.expense.Expense;
import com.rares.budget_management_app.expense.ExpenseRepository;
import com.rares.budget_management_app.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final CurrencyService currencyService;

    public List<BudgetResponse> getBudgets(User currentUser,
                                           String categoryName,
                                           Integer month,
                                           Integer year) {
        List<Budget> budgets = new ArrayList<>();
        Category category = categoryName != null ? getCategoryByName(currentUser, categoryName) : null;

        if (categoryName != null && month != null && year != null) {
            Budget budget = budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                    currentUser.getId(), category.getId(), month, year)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            Error.BUDGET_NOT_FOUND, categoryName, Month.of(month).name(), year));
            budgets.add(budget);

        } else if (categoryName != null && year != null) {
            budgets = budgetRepository.findAllByUserIdAndCategoryIdAndYear(
                    currentUser.getId(), category.getId(), year);

        } else if (categoryName != null) {
            budgets = budgetRepository.findAllByUserIdAndCategoryId(
                    currentUser.getId(), category.getId());

        } else if (month != null && year != null) {
            budgets = budgetRepository.findAllByUserIdAndMonthAndYear(
                    currentUser.getId(), month, year);

        } else if (year != null) {
            budgets = budgetRepository.findAllByUserIdAndYear(
                    currentUser.getId(), year);

        } else if (month != null) {
            int currentYear = LocalDate.now().getYear();
            budgets = budgetRepository.findAllByUserIdAndMonthAndYear(
                    currentUser.getId(), month, currentYear);
        }
        else {
            budgets = budgetRepository.findAllByUserId(currentUser.getId());
        }

        return budgets.stream()
                .map(this::toResponse)
                .toList();
    }

    public BudgetResponse createBudget(User currentUser, BudgetRequest request) {
        int month = request.getMonth() != null
                ? Month.valueOf(request.getMonth().toUpperCase()).getValue()
                : LocalDate.now().getMonthValue();

        int year = request.getYear() != null
                ? request.getYear()
                : LocalDate.now().getYear();

        Category category = getCategoryById(currentUser, request.getCategoryId());

        if (budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                currentUser.getId(), category.getId(), month, year)) {
            throw new DuplicateResourceException(
                    Error.BUDGET_ALREADY_EXISTS,
                    category.getName(), Month.of(month).name(), year);
        }

        String currency = request.getCurrency() != null
                ? request.getCurrency().toUpperCase()
                : "RON";

        Budget budget = Budget.builder()
                .value(request.getValue())
                .currency(currency)
                .month(month)
                .year(year)
                .user(currentUser)
                .category(category)
                .build();

        budgetRepository.save(budget);
        recalculateExpenseRates(currentUser.getId(), category.getId(), month, year, currency);
        return toResponse(budget);
    }

    private void recalculateExpenseRates(int userId, int categoryId, int month, int year, String budgetCurrency) {
        List<Expense> expenses = expenseRepository.findAllByUserIdAndCategoryIdAndMonthAndYearAndCurrencyNot(
                userId, categoryId, month, year, budgetCurrency);

        if (expenses.isEmpty()) return;

        for (Expense expense : expenses) {
            BigDecimal rate = currencyService.getExchangeRate(expense.getCurrency(), budgetCurrency);
            expense.setExchangeRate(rate);
        }

        expenseRepository.saveAll(expenses);
    }

    public BudgetResponse updateBudget(User currentUser, BudgetRequest request) {
        int month = request.getMonth() != null
                ? Month.valueOf(request.getMonth().toUpperCase()).getValue()
                : LocalDate.now().getMonthValue();

        int year = request.getYear() != null
                ? request.getYear()
                : LocalDate.now().getYear();

        LocalDate now = LocalDate.now();
        if (year < now.getYear() || (year == now.getYear() && month < now.getMonthValue())) {
            throw new PastBudgetModificationException(
                    Error.PAST_BUDGET_MODIFICATION, Month.of(month).name(), year);
        }

        Category category = getCategoryById(currentUser, request.getCategoryId());
        Budget budget = budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                currentUser.getId(), category.getId(), month, year)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Error.BUDGET_NOT_FOUND, category.getName(), Month.of(month).name(), year));

        budget.setValue(request.getValue());

        return toResponse(budgetRepository.save(budget));
    }

    private Category getCategoryByName(User currentUser, String categoryName) {
        return categoryRepository.findByUserIdAndName(currentUser.getId(), categoryName)
                .orElseThrow(() -> new ResourceNotFoundException(Error.CATEGORY_NOT_FOUND, categoryName));
    }

    private Category getCategoryById(User currentUser, Integer categoryId) {
        return categoryRepository.findByIdAndUserId(categoryId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(Error.CATEGORY_NOT_FOUND, categoryId));
    }

    private BudgetResponse toResponse(Budget budget) {
        return BudgetResponse.builder()
                .id(budget.getId())
                .categoryName(budget.getCategory().getName())
                .value(budget.getValue())
                .currency(budget.getCurrency())
                .month(Month.of(budget.getMonth()).name())
                .year(budget.getYear())
                .createdAt(budget.getCreatedAt())
                .build();
    }
}
