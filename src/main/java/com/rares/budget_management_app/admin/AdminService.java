package com.rares.budget_management_app.admin;

import com.rares.budget_management_app.admin.dto.AdminCreateUserRequest;
import com.rares.budget_management_app.admin.dto.AdminUpdateUserRequest;
import com.rares.budget_management_app.budget.BudgetService;
import com.rares.budget_management_app.budget.dto.BudgetRequest;
import com.rares.budget_management_app.budget.dto.BudgetResponse;
import com.rares.budget_management_app.category.CategoryService;
import com.rares.budget_management_app.category.dto.CategoryResponse;
import com.rares.budget_management_app.common.exception.DuplicateResourceException;
import com.rares.budget_management_app.common.exception.Error;
import com.rares.budget_management_app.common.exception.ResourceNotFoundException;
import com.rares.budget_management_app.expense.ExpenseService;
import com.rares.budget_management_app.expense.dto.ExpenseRequest;
import com.rares.budget_management_app.expense.dto.ExpenseResponse;
import com.rares.budget_management_app.user.Role;
import com.rares.budget_management_app.user.User;
import com.rares.budget_management_app.user.UserRepository;
import com.rares.budget_management_app.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryService categoryService;
    private final BudgetService budgetService;
    private final ExpenseService expenseService;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toUserResponse)
                .toList();
    }

    public UserResponse getUser(Integer userId) {
        return toUserResponse(getUserById(userId));
    }

    public UserResponse createUser(AdminCreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(Error.EMAIL_ALREADY_EXISTS, request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .build();

        return toUserResponse(userRepository.save(user));
    }

    public UserResponse updateUser(Integer userId, AdminUpdateUserRequest request) {
        User user = getUserById(userId);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException(Error.EMAIL_ALREADY_EXISTS, request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        return toUserResponse(userRepository.save(user));
    }

    public void deleteUser(Integer userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }

    public List<CategoryResponse> getUserCategories(Integer userId) {
        return categoryService.getAllCategories(getUserById(userId));
    }

    public CategoryResponse createCategoryForUser(Integer userId, String categoryName) {
        return categoryService.createCategory(getUserById(userId), categoryName);
    }

    public String deleteCategoryForUser(Integer userId, Integer categoryId) {
        return categoryService.deleteCategory(getUserById(userId), categoryId);
    }

    public List<BudgetResponse> getUserBudgets(Integer userId, String category, Integer month, Integer year) {
        return budgetService.getBudgets(getUserById(userId), category, month, year);
    }

    public BudgetResponse createBudgetForUser(Integer userId, BudgetRequest request) {
        return budgetService.createBudget(getUserById(userId), request);
    }

    public BudgetResponse updateBudgetForUser(Integer userId, BudgetRequest request) {
        return budgetService.updateBudget(getUserById(userId), request);
    }

    public List<ExpenseResponse> getUserExpenses(Integer userId, String category,
                                                  Integer month, Integer year) {
        return expenseService.getExpenses(getUserById(userId), category, month, year, null);
    }

    public ExpenseResponse createExpenseForUser(Integer userId, ExpenseRequest request) {
        return expenseService.createExpense(getUserById(userId), request);
    }

    public ExpenseResponse updateExpenseForUser(Integer userId, Integer expenseId, ExpenseRequest request) {
        return expenseService.updateExpense(getUserById(userId), expenseId, request);
    }

    public void deleteExpenseForUser(Integer userId, Integer expenseId) {
        expenseService.deleteExpense(getUserById(userId), expenseId);
    }

    private User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(Error.USER_NOT_FOUND, userId));
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}