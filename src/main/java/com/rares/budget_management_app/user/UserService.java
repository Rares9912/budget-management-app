package com.rares.budget_management_app.user;

import com.rares.budget_management_app.common.exception.DuplicateResourceException;
import com.rares.budget_management_app.common.exception.Error;
import com.rares.budget_management_app.user.dto.UserProfileRequest;
import com.rares.budget_management_app.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse updateProfile(User currentUser, UserProfileRequest request) {
        if (request.getEmail() != null && !request.getEmail().equals(currentUser.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException(Error.EMAIL_ALREADY_EXISTS, request.getEmail());
            }
            currentUser.setEmail(request.getEmail());
        }

        if (request.getName() != null) {
            currentUser.setName(request.getName());
        }

        if (request.getPassword() != null) {
            currentUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return toResponse(userRepository.save(currentUser));
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}