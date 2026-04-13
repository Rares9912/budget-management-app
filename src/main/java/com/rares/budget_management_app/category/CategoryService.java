package com.rares.budget_management_app.category;

import com.rares.budget_management_app.budget.Budget;
import com.rares.budget_management_app.budget.BudgetRepository;
import com.rares.budget_management_app.category.dto.CategoryRequest;
import com.rares.budget_management_app.category.dto.CategoryResponse;
import com.rares.budget_management_app.common.exception.DuplicateResourceException;
import com.rares.budget_management_app.common.exception.ErrorMessage;
import com.rares.budget_management_app.common.exception.ResourceNotFoundException;
import com.rares.budget_management_app.expense.Expense;
import com.rares.budget_management_app.expense.ExpenseRepository;
import com.rares.budget_management_app.user.User;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    public List<CategoryResponse> getAllCategories(@NonNull User currentUser) {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();

        Map<Integer, BigDecimal> budgetByCategory = budgetRepository
                .findAllByUserIdAndMonthAndYear(currentUser.getId(), month, year)
                .stream()
                .collect(Collectors.toMap(b -> b.getCategory().getId(), Budget::getValue));

        Map<Integer, BigDecimal> spentByCategory = expenseRepository
                .findAllByUserIdAndMonthAndYear(currentUser.getId(), month, year)
                .stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getId(),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getValue, BigDecimal::add)
                ));

        return categoryRepository.findAllByUserId(currentUser.getId())
                .stream()
                .map(c -> toResponse(c, budgetByCategory.get(c.getId()),
                        spentByCategory.getOrDefault(c.getId(), BigDecimal.ZERO)))
                .toList();
    }

    public CategoryResponse getCategory(User currentUser, String categoryName) {

        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        Category category = categoryRepository
                .findByUserIdAndName(currentUser.getId(), categoryName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessage.CATEGORY_NOT_FOUND, categoryName));

        BigDecimal budgetLimit = budgetRepository
                .findByUserIdAndCategoryIdAndMonthAndYear
                        (currentUser.getId(), category.getId(), currentMonth, currentYear)
                .map(Budget::getValue)
                .orElse(null);

        BigDecimal moneySpent = expenseRepository.findAllByUserIdAndCategoryIdAndMonthAndYear(
                currentUser.getId(), category.getId(), currentMonth, currentYear)
                .stream()
                .map(Expense::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return toResponse(category, budgetLimit, moneySpent);
    }

    public CategoryResponse createCategory(User currentUser, String categoryName) {
        if (categoryRepository.existsByUserIdAndName(currentUser.getId(), categoryName)) {
            throw new DuplicateResourceException(ErrorMessage.CATEGORY_ALREADY_EXISTS, categoryName);
        }

        Category category = Category.builder()
                .name(categoryName)
                .user(currentUser)
                .build();

        categoryRepository.save(category);

        return toResponse(category, null, BigDecimal.ZERO);
    }

    public void deleteCategory(User currentUser, String categoryName) {
        Category categoryToDelete = categoryRepository.findByUserIdAndName(currentUser.getId(), categoryName)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.CATEGORY_NOT_FOUND, categoryName));

        categoryRepository.delete(categoryToDelete);
    }

    private CategoryResponse toResponse(Category category,
                                        BigDecimal budgetLimit,
                                        BigDecimal moneySpent) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .createdAt(category.getCreatedAt())
                .monthDetails(getMonthDetails(budgetLimit, moneySpent))
                .build();
    }

    private CategoryResponse.MonthDetails getMonthDetails(BigDecimal budgetLimit,
                                                          BigDecimal moneySpent) {

        if (budgetLimit == null) {
            return CategoryResponse.MonthDetails.builder()
                    .moneySpent(moneySpent)
                    .build();
        }

        BigDecimal moneyRemaining = budgetLimit.subtract(moneySpent);

        double percentageSpent = budgetLimit.compareTo(BigDecimal.ZERO) > 0
                ? moneySpent.divide(budgetLimit, 2, RoundingMode.HALF_UP)
                             .multiply(BigDecimal.valueOf(100))
                             .doubleValue()
                : 0.0;

        return CategoryResponse.MonthDetails.builder()
                .budgetLimit(budgetLimit)
                .moneySpent(moneySpent)
                .moneyRemaining(moneyRemaining)
                .percentageSpent(percentageSpent)
                .build();
    }
}
