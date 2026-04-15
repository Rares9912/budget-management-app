package com.rares.budget_management_app.expense;

import com.rares.budget_management_app.budget.Budget;
import com.rares.budget_management_app.budget.BudgetRepository;
import com.rares.budget_management_app.category.Category;
import com.rares.budget_management_app.category.CategoryRepository;
import com.rares.budget_management_app.common.currency.CurrencyService;
import com.rares.budget_management_app.common.exception.ResourceNotFoundException;
import com.rares.budget_management_app.expense.dto.ExpenseRequest;
import com.rares.budget_management_app.expense.dto.ExpenseResponse;
import com.rares.budget_management_app.user.Role;
import com.rares.budget_management_app.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock private ExpenseRepository expenseRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private BudgetRepository budgetRepository;
    @Mock private CurrencyService currencyService;

    @InjectMocks
    private ExpenseService expenseService;

    private User user;
    private Category food;
    private final LocalDate DATE = LocalDate.now();

    @BeforeEach
    void setUp() {
        user = User.builder().id(1).email("john@test.com").name("John").role(Role.USER).build();
        food = Category.builder().id(10).name("Food").user(user).createdAt(LocalDateTime.now()).build();
    }

    @Test
    void getExpenses_returnsAllExpenses_whenNoFiltersProvided() {
        Expense expense = buildExpense(1, new BigDecimal("100.00"), "RON", BigDecimal.ONE);
        when(expenseRepository.findAllByUserId(user.getId())).thenReturn(List.of(expense));

        List<ExpenseResponse> result = expenseService.getExpenses(user, null, null, null, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void getExpenses_filtersByDate() {
        Expense expense = buildExpense(1, new BigDecimal("50.00"), "RON", BigDecimal.ONE);
        when(expenseRepository.findAllByUserIdAndExpenseDate(user.getId(), DATE))
                .thenReturn(List.of(expense));

        List<ExpenseResponse> result = expenseService.getExpenses(user, null, null, null, DATE);

        assertThat(result).hasSize(1);
    }

    @Test
    void getExpenses_filtersByMonthAndYear() {
        when(expenseRepository.findAllByUserIdAndMonthAndYear(user.getId(), 4, 2026))
                .thenReturn(List.of());

        List<ExpenseResponse> result = expenseService.getExpenses(user, null, 4, 2026, null);

        assertThat(result).isEmpty();
        verify(expenseRepository).findAllByUserIdAndMonthAndYear(user.getId(), 4, 2026);
    }

    @Test
    void createExpense_storesRONExpenseWithConversionRateOne_whenNoBudgetExists() {
        ExpenseRequest request = new ExpenseRequest(10, new BigDecimal("100.00"), "Lunch", DATE, "RON");

        when(categoryRepository.findByIdAndUserId(10, user.getId())).thenReturn(Optional.of(food));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));
        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(Optional.empty());
        when(currencyService.getExchangeRate("RON", "RON")).thenReturn(BigDecimal.ONE);

        ExpenseResponse result = expenseService.createExpense(user, request);
        System.out.println(result);

        assertThat(result.getValue()).isEqualByComparingTo("100.00");
        assertThat(result.getCurrency()).isEqualTo("RON");
        assertThat(result.getExchangeRate()).isEqualByComparingTo("1");
    }

    @Test
    void createExpense_convertsToBudgetCurrency_whenBudgetExistsInDifferentCurrency() {
        ExpenseRequest request = new ExpenseRequest(10, new BigDecimal("100.00"), "Lunch", DATE, "USD");
        Budget budget = buildBudget(food, new BigDecimal("500.00"), "EUR", 4, 2026);

        when(categoryRepository.findByIdAndUserId(10, user.getId())).thenReturn(Optional.of(food));
        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                user.getId(), food.getId(), 4, 2026)).thenReturn(Optional.of(budget));
        when(currencyService.getExchangeRate("USD", "EUR")).thenReturn(new BigDecimal("0.920000"));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));
        when(expenseRepository.findAllByUserIdAndCategoryIdAndMonthAndYear(
                anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(List.of());

        ExpenseResponse result = expenseService.createExpense(user, request);

        assertThat(result.getExchangeRate()).isEqualByComparingTo("0.920000");
        assertThat(result.getCurrency()).isEqualTo("USD");
        verify(currencyService).getExchangeRate("USD", "EUR");
    }

    @Test
    void createExpense_setsConversionRateOne_whenExpenseAndBudgetHaveSameCurrency() {
        ExpenseRequest request = new ExpenseRequest(10, new BigDecimal("100.00"), "Lunch", DATE, "EUR");
        Budget budget = buildBudget(food, new BigDecimal("500.00"), "EUR", 4, 2026);

        when(categoryRepository.findByIdAndUserId(10, user.getId())).thenReturn(Optional.of(food));
        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                user.getId(), food.getId(), 4, 2026)).thenReturn(Optional.of(budget));
        when(currencyService.getExchangeRate("EUR", "EUR")).thenReturn(BigDecimal.ONE);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));
        when(expenseRepository.findAllByUserIdAndCategoryIdAndMonthAndYear(
                anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(List.of());

        ExpenseResponse result = expenseService.createExpense(user, request);

        assertThat(result.getExchangeRate()).isEqualByComparingTo("1");
    }

    @Test
    void createExpense_returnsBudgetWarning_whenBudgetExceeded() {
        ExpenseRequest request = new ExpenseRequest(10, new BigDecimal("600.00"), "Luxury", DATE, "RON");
        Budget budget = buildBudget(food, new BigDecimal("500.00"), "RON", 4, 2026);
        Expense existingExpense = buildExpense(2, new BigDecimal("600.00"), "RON", BigDecimal.ONE);

        when(categoryRepository.findByIdAndUserId(10, user.getId())).thenReturn(Optional.of(food));
        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                user.getId(), food.getId(), 4, 2026)).thenReturn(Optional.of(budget));
        when(currencyService.getExchangeRate("RON", "RON")).thenReturn(BigDecimal.ONE);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));
        when(expenseRepository.findAllByUserIdAndCategoryIdAndMonthAndYear(
                anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(List.of(existingExpense));

        ExpenseResponse result = expenseService.createExpense(user, request);

        assertThat(result.getBudgetWarning()).isNotNull().contains("Warning!");
    }

    @Test
    void createExpense_throwsResourceNotFoundException_whenCategoryNotFound() {
        ExpenseRequest request = new ExpenseRequest(99, new BigDecimal("100.00"), null, DATE, "RON");

        when(categoryRepository.findByIdAndUserId(99, user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.createExpense(user, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── updateExpense ─────────────────────────────────────────────────────────

    @Test
    void updateExpense_updatesFieldsAndReturnsResponse() {
        Expense existing = buildExpense(1, new BigDecimal("100.00"), "RON", BigDecimal.ONE);
        ExpenseRequest request = new ExpenseRequest(10, new BigDecimal("200.00"), "Updated", DATE, "RON");

        when(expenseRepository.findByIdAndUserId(1, user.getId())).thenReturn(Optional.of(existing));
        when(categoryRepository.findByIdAndUserId(10, user.getId())).thenReturn(Optional.of(food));
        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(Optional.empty());
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        ExpenseResponse result = expenseService.updateExpense(user, 1, request);

        assertThat(result.getValue()).isEqualByComparingTo("200.00");
        assertThat(result.getDescription()).isEqualTo("Updated");
    }

    @Test
    void updateExpense_throwsResourceNotFoundException_whenExpenseNotFound() {
        ExpenseRequest request = new ExpenseRequest(10, new BigDecimal("100.00"), null, DATE, "RON");

        when(expenseRepository.findByIdAndUserId(99, user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.updateExpense(user, 99, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── deleteExpense ─────────────────────────────────────────────────────────

    @Test
    void deleteExpense_deletesExpense_whenFound() {
        Expense expense = buildExpense(1, new BigDecimal("100.00"), "RON", BigDecimal.ONE);
        when(expenseRepository.findByIdAndUserId(1, user.getId())).thenReturn(Optional.of(expense));

        expenseService.deleteExpense(user, 1);

        verify(expenseRepository).delete(expense);
    }

    @Test
    void deleteExpense_throwsResourceNotFoundException_whenExpenseNotFound() {
        when(expenseRepository.findByIdAndUserId(99, user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.deleteExpense(user, 99))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(expenseRepository, never()).delete(any());
    }

    private Expense buildExpense(int id, BigDecimal value, String currency, BigDecimal rate) {
        return Expense.builder()
                .id(id)
                .value(value)
                .currency(currency)
                .exchangeRate(rate)
                .expenseDate(DATE)
                .category(food)
                .user(user)
                .build();
    }

    private Budget buildBudget(Category category, BigDecimal value, String currency, int month, int year) {
        return Budget.builder()
                .id(1)
                .value(value)
                .currency(currency)
                .month(month)
                .year(year)
                .category(category)
                .user(user)
                .build();
    }
}