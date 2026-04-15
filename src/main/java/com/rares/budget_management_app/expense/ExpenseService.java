package com.rares.budget_management_app.expense;

import com.rares.budget_management_app.budget.Budget;
import com.rares.budget_management_app.budget.BudgetRepository;
import com.rares.budget_management_app.category.Category;
import com.rares.budget_management_app.category.CategoryRepository;
import com.rares.budget_management_app.common.currency.CurrencyService;
import com.rares.budget_management_app.common.exception.ResourceNotFoundException;
import com.rares.budget_management_app.expense.dto.ExpenseRequest;
import com.rares.budget_management_app.expense.dto.ExpenseResponse;
import com.rares.budget_management_app.user.User;
import com.rares.budget_management_app.common.exception.Error;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final CurrencyService currencyService;

    public List<ExpenseResponse> getExpenses(User currentUser,
                                             String categoryName,
                                             Integer month,
                                             Integer year,
                                             LocalDate date) {
        List<Expense> expenses;

        Category category = categoryName != null
                ? getCategoryByName(currentUser, categoryName)
                : null;

        if (date != null) {
            expenses = expenseRepository.findAllByUserIdAndExpenseDate(
                    currentUser.getId(), date);

        } else if (category != null && month != null && year != null) {
            expenses = expenseRepository.findAllByUserIdAndCategoryIdAndMonthAndYear(
                    currentUser.getId(), category.getId(), month, year);

        } else if (category != null && year != null) {
            expenses = expenseRepository.findAllByUserIdAndCategoryIdAndYear(
                    currentUser.getId(), category.getId(), year);

        } else if (category != null) {
            expenses = expenseRepository.findAllByUserIdAndCategoryId(
                    currentUser.getId(), category.getId());

        } else if (month != null && year != null) {
            expenses = expenseRepository.findAllByUserIdAndMonthAndYear(
                    currentUser.getId(), month, year);

        } else if (year != null) {
            expenses = expenseRepository.findAllByUserIdAndYear(
                    currentUser.getId(), year);

        } else if (month != null) {
            expenses = expenseRepository.findAllByUserIdAndMonthAndYear(
                    currentUser.getId(), month, LocalDate.now().getYear());

        } else {
            expenses = expenseRepository.findAllByUserId(currentUser.getId());
        }

        return expenses.stream()
                .map(e -> toResponse(e, null))
                .toList();
    }

    public ExpenseResponse createExpense(User currentUser, ExpenseRequest request) {
        Category category = getCategoryById(currentUser, request.getCategoryId());
        String expenseCurrency = request.getCurrency() != null
                ? request.getCurrency().toUpperCase()
                : "RON";

        BigDecimal conversionRate = getExchangeRate(
                currentUser, category, request.getExpenseDate(), expenseCurrency);

        Expense expense = Expense.builder()
                .value(request.getValue())
                .currency(expenseCurrency)
                .exchangeRate(conversionRate)
                .description(request.getDescription())
                .expenseDate(request.getExpenseDate())
                .category(category)
                .user(currentUser)
                .build();

        expenseRepository.save(expense);

        String warning = checkBudget(currentUser, category, request.getExpenseDate());
        return toResponse(expense, warning);
    }

    public ExpenseResponse updateExpense(User currentUser,
                                         Integer expenseId,
                                         ExpenseRequest request) {
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        Error.EXPENSE_NOT_FOUND, expenseId));

        Category category = getCategoryById(currentUser, request.getCategoryId());
        String expenseCurrency = request.getCurrency() != null
                ? request.getCurrency().toUpperCase()
                : "RON";

        BigDecimal exchangeRate = getExchangeRate(
                currentUser, category, request.getExpenseDate(), expenseCurrency);

        expense.setValue(request.getValue());
        expense.setCurrency(expenseCurrency);
        expense.setExchangeRate(exchangeRate);
        expense.setDescription(request.getDescription());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setCategory(category);

        expenseRepository.save(expense);

        String warning = checkBudget(currentUser, category, request.getExpenseDate());
        return toResponse(expense, warning);
    }


    public void deleteExpense(User currentUser, Integer expenseId) {
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        Error.EXPENSE_NOT_FOUND, expenseId));

        expenseRepository.delete(expense);
    }

    private String checkBudget(User currentUser, Category category, LocalDate expenseDate) {
        int month = expenseDate.getMonthValue();
        int year = expenseDate.getYear();

        Optional<Budget> budgetOpt = budgetRepository
                .findByUserIdAndCategoryIdAndMonthAndYear(
                        currentUser.getId(), category.getId(), month, year);

        if (budgetOpt.isEmpty()) return null;

        Budget budget = budgetOpt.get();

        BigDecimal totalSpent = expenseRepository
                .findAllByUserIdAndCategoryIdAndMonthAndYear(
                        currentUser.getId(), category.getId(), month, year)
                .stream()
                .map(e -> e.getValue().multiply(e.getExchangeRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalSpent.compareTo(budget.getValue()) > 0) {
            BigDecimal overspent = totalSpent.subtract(budget.getValue());
            return String.format(
                    "Warning! Budget exceeded for category %s on %s %d. " +
                            "Budget: %.2f RON, Spent: %.2f RON, Overspent: %.2f RON",
                    category.getName(),
                    Month.of(month).name(),
                    year,
                    budget.getValue(),
                    totalSpent,
                    overspent
            );
        }

        return null;
    }

    private Category getCategoryByName(User currentUser, String categoryName) {
        return categoryRepository.findByUserIdAndName(currentUser.getId(), categoryName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Error.CATEGORY_NOT_FOUND, categoryName));
    }

    private Category getCategoryById(User currentUser, Integer categoryId) {
        return categoryRepository.findByIdAndUserId(categoryId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        Error.CATEGORY_NOT_FOUND, categoryId));
    }

    private BigDecimal getExchangeRate(User currentUser, Category category,
                                       LocalDate expenseDate, String expenseCurrency) {
        int month = expenseDate.getMonthValue();
        int year = expenseDate.getYear();

        return budgetRepository
                .findByUserIdAndCategoryIdAndMonthAndYear(
                        currentUser.getId(), category.getId(), month, year)
                .map(budget -> currencyService.getExchangeRate(expenseCurrency, budget.getCurrency()))
                .orElse(currencyService.getExchangeRate(expenseCurrency, "RON"));
    }

    private ExpenseResponse toResponse(Expense expense, String warning) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .categoryId(expense.getCategory().getId())
                .categoryName(expense.getCategory().getName())
                .value(expense.getValue())
                .currency(expense.getCurrency())
                .exchangeRate(expense.getExchangeRate())
                .description(expense.getDescription())
                .expenseDate(expense.getExpenseDate())
                .createdAt(expense.getCreatedAt())
                .budgetWarning(warning)
                .build();
    }
}