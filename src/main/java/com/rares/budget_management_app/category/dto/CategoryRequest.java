package com.rares.budget_management_app.category.dto;

import com.rares.budget_management_app.common.Constants;
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
    @NotBlank(message = Constants.CATEGORY_NAME_REQUIRED)
    @Size(max = 30, message = Constants.CATEGORY_NAME_MIN_CHARACTERS)
    private String name;
}
