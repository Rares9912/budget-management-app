package com.rares.budget_management_app.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
    List<Expense> findAllByUserId(Integer userId);
    List<Expense> findAllByUserIdAndCategoryId(Integer userId, Integer categoryId);
    List<Expense> findAllByUserIdAndExpenseDate(Integer userId, LocalDate expenseDate);


    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId " +
            "AND YEAR(e.expenseDate) = :year AND MONTH(e.expenseDate) = :month")
    List<Expense> findAllByUserIdAndMonthAndYear(
            @Param("userId") Integer userId,
            @Param("month") Integer month,
            @Param("year") Integer year);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND YEAR(e.expenseDate) = :year")
    List<Expense> findAllByUserIdAndYear(
            @Param("userId") Integer userId,
            @Param("year") Integer year);


    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.category.id = :categoryId " +
            "AND YEAR(e.expenseDate) = :year")
    List<Expense> findAllByUserIdAndCategoryIdAndYear(
            @Param("userId") Integer userId,
            @Param("categoryId") Integer categoryId,
            @Param("year") Integer year);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.category.id = :categoryId " +
            "AND YEAR(e.expenseDate) = :year AND MONTH(e.expenseDate) = :month")
    List<Expense> findAllByUserIdAndCategoryIdAndMonthAndYear(
            @Param("userId") Integer userId,
            @Param("categoryId") Integer categoryId,
            @Param("month") Integer month,
            @Param("year") Integer year);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.category.id = :categoryId " +
            "AND YEAR(e.expenseDate) = :year AND MONTH(e.expenseDate) = :month " +
            "AND e.currency != :currency")
    List<Expense> findAllByUserIdAndCategoryIdAndMonthAndYearAndCurrencyNot(
            @Param("userId") Integer userId,
            @Param("categoryId") Integer categoryId,
            @Param("month") Integer month,
            @Param("year") Integer year,
            @Param("currency") String currency);

    Optional<Expense> findByIdAndUserId(Integer expenseId, Integer userId);
}