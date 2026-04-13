package com.rares.budget_management_app.category;

import com.rares.budget_management_app.category.dto.CategoryRequest;
import com.rares.budget_management_app.category.dto.CategoryResponse;
import com.rares.budget_management_app.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(categoryService.getAllCategories(user));
    }

    @GetMapping("/{name}")
    public ResponseEntity<CategoryResponse> getCategory(@AuthenticationPrincipal User user,
                                                        @PathVariable String name) {
        return ResponseEntity.ok(categoryService.getCategory(user, name));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@AuthenticationPrincipal User user,
                                                           @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.createCategory(user, request.getName()));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<String> deleteCategory(@AuthenticationPrincipal User user,
                                                 @PathVariable String name) {
        categoryService.deleteCategory(user, name);
        return ResponseEntity.ok("Categoria " + name + " a fost stearsa cu succes!");
    }

}
