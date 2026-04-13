package com.rares.budget_management_app.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Numele este obligatoriu")
    private String name;

    @NotBlank(message = "Email-ul este obligatoriu")
    @Email(message = "Format email invalid")
    private String email;

    @NotBlank(message = "Parola este obligatorie")
    @Size(min = 8, message = "Parola trebuie sa aiba minim 8 caractere")
    private String password;
}
