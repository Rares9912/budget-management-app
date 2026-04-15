package com.rares.budget_management_app.category;

import com.rares.budget_management_app.category.dto.CategoryRequest;
import com.rares.budget_management_app.category.dto.CategoryResponse;
import com.rares.budget_management_app.user.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Category")
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(categoryService.getAllCategories(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(@AuthenticationPrincipal User user,
                                                        @PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.getCategory(user, id));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@AuthenticationPrincipal User user,
                                                           @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.createCategory(user, request.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@AuthenticationPrincipal User user,
                                                 @PathVariable Integer id) {
        String deletedName = categoryService.deleteCategory(user, id);
        return ResponseEntity.ok("Category " + deletedName + " was successfully deleted!");
    }

}
