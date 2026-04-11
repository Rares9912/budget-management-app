package com.rares.budget_management_app.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
    List<Expense> findAllByUserId(Integer userId);
    List<Expense> findAllByUserIdAndCategoryId(Integer userId, Integer categoryId);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId " +
            "AND YEAR(e.expenseDate) = :year AND MONTH(e.expenseDate) = :month " +
            "AND DAY(e.expenseDate) = :day")
    List<Expense> findAllByUserIdAndYearAndMonthAndDay(
            @Param("userId") Integer userId,
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("day") Integer day
    );

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
}