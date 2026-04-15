package com.rares.budget_management_app.auth.dto;

import com.rares.budget_management_app.common.Constants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = Constants.EMAIL_REQUIRED)
    @Email(message = Constants.INVALID_EMAIL_FORMAT)
    private String email;

    @NotBlank(message = Constants.PASSWORD_REQUIRED)
    private String password;
}
