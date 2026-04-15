package com.rares.budget_management_app.budget;

import com.rares.budget_management_app.budget.dto.BudgetRequest;
import com.rares.budget_management_app.budget.dto.BudgetResponse;
import com.rares.budget_management_app.category.Category;
import com.rares.budget_management_app.category.CategoryRepository;
import com.rares.budget_management_app.common.currency.CurrencyService;
import com.rares.budget_management_app.common.exception.DuplicateResourceException;
import com.rares.budget_management_app.common.exception.PastBudgetModificationException;
import com.rares.budget_management_app.common.exception.ResourceNotFoundException;
import com.rares.budget_management_app.expense.Expense;
import com.rares.budget_management_app.expense.ExpenseRepository;
import com.rares.budget_management_app.user.Role;
import com.rares.budget_management_app.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock private BudgetRepository budgetRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ExpenseRepository expenseRepository;
    @Mock private CurrencyService currencyService;

    @InjectMocks
    private BudgetService budgetService;

    private User user;
    private Category food;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1).email("john@test.com").name("John").role(Role.USER).build();
        food = Category.builder().id(10).name("Food").user(user).createdAt(LocalDateTime.now()).build();
    }

    @Test
    void getBudgets_returnsAllBudgets_whenNoFiltersProvided() {
        Budget budget = buildBudget(1, food, new BigDecimal("500.00"), 4, 2026);
        when(budgetRepository.findAllByUserId(user.getId())).thenReturn(List.of(budget));

        List<BudgetResponse> result = budgetService.getBudgets(user, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValue()).isEqualByComparingTo("500.00");
    }

    @Test
    void getBudgets_filtersByMonthAndYear() {
        Budget budget = buildBudget(1, food, new BigDecimal("500.00"), 4, 2026);
        when(budgetRepository.findAllByUserIdAndMonthAndYear(user.getId(), 4, 2026))
                .thenReturn(List.of(budget));

        List<BudgetResponse> result = budgetService.getBudgets(user, null, 4, 2026);

        assertThat(result).hasSize(1);
    }

    @Test
    void createBudget_savesAndReturnsResponse() {
        BudgetRequest request = new BudgetRequest(10, new BigDecimal("500.00"), "RON", "APRIL", 2099);

        when(categoryRepository.findByIdAndUserId(10, user.getId())).thenReturn(Optional.of(food));
        when(budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                user.getId(), food.getId(), 4, 2099)).thenReturn(false);
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));
        when(expenseRepository.findAllByUserIdAndCategoryIdAndMonthAndYearAndCurrencyNot(
                user.getId(), food.getId(), 4, 2099, "RON")).thenReturn(List.of());

        BudgetResponse result = budgetService.createBudget(user, request);

        assertThat(result.getValue()).isEqualByComparingTo("500.00");
        assertThat(result.getCurrency()).isEqualTo("RON");
        assertThat(result.getCategoryName()).isEqualTo("Food");
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void createBudget_defaultsToCurrencyRON_whenNotProvided() {
        BudgetRequest request = new BudgetRequest(10, new BigDecimal("200.00"), null, "JANUARY", 2099);

        when(categoryRepository.findByIdAndUserId(10, user.getId())).thenReturn(Optional.of(food));
        when(budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(false);
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));
        when(expenseRepository.findAllByUserIdAndCategoryIdAndMonthAndYearAndCurrencyNot(
                anyInt(), anyInt(), anyInt(), anyInt(), anyString())).thenReturn(List.of());

        BudgetResponse result = budgetService.createBudget(user, request);

        assertThat(result.getCurrency()).isEqualTo("RON");
    }

    @Test
    void createBudget_recalculatesExchangeRates_whenExistingExpensesFoundAndBudgetCurrencyIsEUR() {
        BudgetRequest request = new BudgetRequest(10, new BigDecimal("500.00"), "EUR", "APRIL", 2026);
        Expense existing = Expense.builder()
                .id(1).value(new BigDecimal("100.00")).currency("RON")
                .exchangeRate(BigDecimal.ONE).expenseDate(java.time.LocalDate.of(2026, 4, 1))
                .category(food).user(user).build();

        when(categoryRepository.findByIdAndUserId(10, user.getId())).thenReturn(Optional.of(food));
        when(budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                user.getId(), food.getId(), 4, 2026)).thenReturn(false);
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));
        when(expenseRepository.findAllByUserIdAndCategoryIdAndMonthAndYearAndCurrencyNot(
                user.getId(), food.getId(), 4, 2026, "EUR")).thenReturn(List.of(existing));
        when(currencyService.getExchangeRate("RON", "EUR")).thenReturn(new BigDecimal("0.200000"));

        budgetService.createBudget(user, request);

        assertThat(existing.getExchangeRate()).isEqualByComparingTo("0.200000");
        verify(expenseRepository).saveAll(anyList());
    }

    @Test
    void createBudget_throwsDuplicateResourceException_whenBudgetAlreadyExists() {
        BudgetRequest request = new BudgetRequest(10, new BigDecimal("500.00"), "RON", "APRIL", 2099);

        when(categoryRepository.findByIdAndUserId(10, user.getId())).thenReturn(Optional.of(food));
        when(budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                user.getId(), food.getId(), 4, 2099)).thenReturn(true);

        assertThatThrownBy(() -> budgetService.createBudget(user, request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(budgetRepository, never()).save(any());
    }

    @Test
    void createBudget_throwsResourceNotFoundException_whenCategoryNotFound() {
        BudgetRequest request = new BudgetRequest(99, new BigDecimal("500.00"), "RON", "APRIL", 2099);

        when(categoryRepository.findByIdAndUserId(99, user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.createBudget(user, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateBudget_updatesValueAndReturnsResponse() {
        BudgetRequest request = new BudgetRequest(10, new BigDecimal("800.00"), "EUR", "JANUARY", 2099);
        Budget existing = buildBudget(1, food, new BigDecimal("500.00"), 1, 2099);

        when(categoryRepository.findByIdAndUserId(10, user.getId())).thenReturn(Optional.of(food));
        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                user.getId(), food.getId(), 1, 2099)).thenReturn(Optional.of(existing));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));

        BudgetResponse result = budgetService.updateBudget(user, request);

        assertThat(result.getValue()).isEqualByComparingTo("800.00");
    }

    @Test
    void updateBudget_throwsPastBudgetModificationException_whenBudgetIsInThePast() {
        BudgetRequest request = new BudgetRequest(10, new BigDecimal("500.00"), "RON", "JANUARY", 2020);

        assertThatThrownBy(() -> budgetService.updateBudget(user, request))
                .isInstanceOf(PastBudgetModificationException.class);

        verify(budgetRepository, never()).save(any());
    }

    @Test
    void updateBudget_throwsResourceNotFoundException_whenBudgetDoesNotExist() {
        BudgetRequest request = new BudgetRequest(10, new BigDecimal("500.00"), "RON", "JANUARY", 2099);

        when(categoryRepository.findByIdAndUserId(10, user.getId())).thenReturn(Optional.of(food));
        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                user.getId(), food.getId(), 1, 2099)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.updateBudget(user, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Budget buildBudget(int id, Category category, BigDecimal value, int month, int year) {
        return Budget.builder()
                .id(id)
                .value(value)
                .currency("RON")
                .month(month)
                .year(year)
                .category(category)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();
    }
}