package com.rares.budget_management_app.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequest {
    @NotBlank(message = "Numele categoriei este obligatoriu")
    @Size(max = 30, message = "Numele categoriei nu poate depasi 30 de caractere")
    private String name;
}
