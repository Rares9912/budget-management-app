package com.rares.budget_management_app.user.dto;

import com.rares.budget_management_app.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Integer id;
    private String email;
    private String name;
    private Role role;
    private LocalDateTime createdAt;
}