package com.rares.budget_management_app.admin.dto;

import com.rares.budget_management_app.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateUserRequest {

    @NotBlank(message = "Email-ul este obligatoriu")
    @Email(message = "Email invalid")
    private String email;

    @NotBlank(message = "Numele este obligatoriu")
    private String name;

    @NotBlank(message = "Parola este obligatorie")
    private String password;

    private Role role;
}