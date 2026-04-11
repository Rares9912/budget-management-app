package com.rares.budget_management_app.budget;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Integer> {
    List<Budget> findAllByUserId(Integer userId);
    List<Budget> findAllByUserIdAndMonthAndYear(Integer userId, Integer month, Integer year);
    List<Budget> findAllByUserIdAndYear(Integer userId, Integer year);
    Optional<Budget> findByUserIdAndCategoryIdAndMonthAndYear(Integer userId, Integer categoryId, Integer month, Integer year);
    boolean existsByUserIdAndCategoryIdAndMonthAndYear(Integer userId, Integer categoryId, Integer month, Integer year);
}
