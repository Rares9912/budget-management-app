package com.rares.budget_management_app.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findAllByUserId(Integer userId);
    Optional<Category> findByIdAndUserId(Integer id, Integer userId);
    Optional<Category> findByUserIdAndName(Integer userId, String name);
    boolean existsByUserIdAndName(Integer userId, String name);
    boolean existsByIdAndUserId(Integer id, Integer userId);
}
