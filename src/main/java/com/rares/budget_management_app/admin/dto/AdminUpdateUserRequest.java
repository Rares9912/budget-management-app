package com.rares.budget_management_app.admin.dto;

import com.rares.budget_management_app.user.Role;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateUserRequest {

    @Email(message = "Email invalid")
    private String email;

    private String name;

    private String password;

    private Role role;
}