package com.rares.budget_management_app.category;

import com.rares.budget_management_app.budget.Budget;
import com.rares.budget_management_app.budget.BudgetRepository;
import com.rares.budget_management_app.category.dto.CategoryResponse;
import com.rares.budget_management_app.common.exception.DuplicateResourceException;
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
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1)
                .email("test@test.com")
                .name("Test User")
                .role(Role.USER)
                .build();
    }

    @Test
    void getAllCategories_returnsEmptyList_whenNoCategoriesExist() {
        when(categoryRepository.findAllByUserId(user.getId())).thenReturn(List.of());
        when(budgetRepository.findAllByUserIdAndMonthAndYear(anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(expenseRepository.findAllByUserIdAndMonthAndYear(anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of());

        List<CategoryResponse> result = categoryService.getAllCategories(user);

        assertThat(result).isEmpty();
    }

    @Test
    void getAllCategories_returnsCategoriesWithMonthDetails_whenBudgetAndExpensesExist() {
        Category food = buildCategory(1, "Food");
        Budget budget = buildBudget(food, new BigDecimal("500.00"));
        Expense expense = buildExpense(food, new BigDecimal("200.00"));

        when(categoryRepository.findAllByUserId(user.getId())).thenReturn(List.of(food));
        when(budgetRepository.findAllByUserIdAndMonthAndYear(anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(budget));
        when(expenseRepository.findAllByUserIdAndMonthAndYear(anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(expense));

        List<CategoryResponse> result = categoryService.getAllCategories(user);

        assertThat(result).hasSize(1);
        CategoryResponse.MonthDetails details = result.getFirst().getMonthDetails();
        assertThat(details.getBudgetLimit()).isEqualByComparingTo("500.00");
        assertThat(details.getMoneySpent()).isEqualByComparingTo("200.00");
        assertThat(details.getMoneyRemaining()).isEqualByComparingTo("300.00");
    }

    @Test
    void getAllCategories_returnsOnlyMoneySpent_whenNoBudgetExists() {
        Category food = buildCategory(1, "Food");
        Expense expense = buildExpense(food, new BigDecimal("150.00"));

        when(categoryRepository.findAllByUserId(user.getId())).thenReturn(List.of(food));
        when(budgetRepository.findAllByUserIdAndMonthAndYear(anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(expenseRepository.findAllByUserIdAndMonthAndYear(anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(expense));

        List<CategoryResponse> result = categoryService.getAllCategories(user);

        CategoryResponse.MonthDetails details = result.getFirst().getMonthDetails();
        assertThat(details.getBudgetLimit()).isNull();
        assertThat(details.getMoneySpent()).isEqualByComparingTo("150.00");
        assertThat(details.getMoneyRemaining()).isNull();
    }

    @Test
    void getAllCategories_sumsMultipleExpenses() {
        Category food = buildCategory(1, "Food");
        Expense e1 = buildExpense(food, new BigDecimal("100.00"));
        Expense e2 = buildExpense(food, new BigDecimal("75.50"));

        when(categoryRepository.findAllByUserId(user.getId())).thenReturn(List.of(food));
        when(budgetRepository.findAllByUserIdAndMonthAndYear(anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(expenseRepository.findAllByUserIdAndMonthAndYear(anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(e1, e2));

        List<CategoryResponse> result = categoryService.getAllCategories(user);

        assertThat(result.getFirst().getMonthDetails().getMoneySpent())
                .isEqualByComparingTo("175.50");
    }

    @Test
    void getCategory_returnsResponse_whenCategoryExists() {
        Category food = buildCategory(1, "Food");
        Budget budget = buildBudget(food, new BigDecimal("300.00"));
        Expense expense = buildExpense(food, new BigDecimal("100.00"));

        when(categoryRepository.findByIdAndUserId(1, user.getId()))
                .thenReturn(Optional.of(food));
        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(Optional.of(budget));
        when(expenseRepository.findAllByUserIdAndCategoryIdAndMonthAndYear(
                anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(expense));

        CategoryResponse result = categoryService.getCategory(user, 1);

        assertThat(result.getName()).isEqualTo("Food");
        assertThat(result.getMonthDetails().getBudgetLimit()).isEqualByComparingTo("300.00");
        assertThat(result.getMonthDetails().getMoneySpent()).isEqualByComparingTo("100.00");
        assertThat(result.getMonthDetails().getMoneyRemaining()).isEqualByComparingTo("200.00");
    }

    @Test
    void getCategory_returnsNullBudgetLimit_whenNoBudgetSet() {
        Category food = buildCategory(1, "Food");

        when(categoryRepository.findByIdAndUserId(1, user.getId()))
                .thenReturn(Optional.of(food));
        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());
        when(expenseRepository.findAllByUserIdAndCategoryIdAndMonthAndYear(
                anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of());

        CategoryResponse result = categoryService.getCategory(user, 1);

        assertThat(result.getMonthDetails().getBudgetLimit()).isNull();
        assertThat(result.getMonthDetails().getMoneySpent()).isEqualByComparingTo("0.00");
    }

    @Test
    void getCategory_throwsResourceNotFoundException_whenCategoryNotFound() {
        when(categoryRepository.findByIdAndUserId(99, user.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategory(user, 99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createCategory_savesAndReturnsResponse() {
        when(categoryRepository.existsByUserIdAndName(user.getId(), "Transport"))
                .thenReturn(false);
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(inv -> {
                    Category c = inv.getArgument(0);
                    c.setCreatedAt(LocalDateTime.now());
                    return c;
                });

        CategoryResponse result = categoryService.createCategory(user, "Transport");

        assertThat(result.getName()).isEqualTo("Transport");
        assertThat(result.getMonthDetails().getBudgetLimit()).isNull();
        assertThat(result.getMonthDetails().getMoneySpent()).isEqualByComparingTo("0.00");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_throwsDuplicateResourceException_whenNameAlreadyExists() {
        when(categoryRepository.existsByUserIdAndName(user.getId(), "Food"))
                .thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(user, "Food"))
                .isInstanceOf(DuplicateResourceException.class);

        // verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteCategory_deletesAndReturnsCategoryName() {
        Category food = buildCategory(1, "Food");

        when(categoryRepository.findByIdAndUserId(1, user.getId()))
                .thenReturn(Optional.of(food));

        String result = categoryService.deleteCategory(user, 1);

        assertThat(result).isEqualTo("Food");
        verify(categoryRepository).delete(food);
    }

    @Test
    void deleteCategory_throwsResourceNotFoundException_whenCategoryNotFound() {
        when(categoryRepository.findByIdAndUserId(99, user.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(user, 99))
                .isInstanceOf(ResourceNotFoundException.class);

        // verify(categoryRepository, never()).delete(any());
    }

    private Category buildCategory(int id, String name) {
        return Category.builder()
                .id(id)
                .name(name)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Budget buildBudget(Category category, BigDecimal value) {
        return Budget.builder()
                .id(1)
                .value(value)
                .currency("RON")
                .month(1)
                .year(2026)
                .category(category)
                .user(user)
                .build();
    }

    private Expense buildExpense(Category category, BigDecimal value) {
        return Expense.builder()
                .id(1)
                .value(value)
                .currency("RON")
                .exchangeRate(BigDecimal.ONE)
                .category(category)
                .user(user)
                .build();
    }
}