package com.rares.budget_management_app.user.dto;

import com.rares.budget_management_app.common.Constants;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {

    @Email(message = Constants.INVALID_EMAIL_FORMAT)
    private String email;

    private String name;

    private String password;
}