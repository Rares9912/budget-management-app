package com.rares.budget_management_app.auth;

import com.rares.budget_management_app.auth.dto.AuthResponse;
import com.rares.budget_management_app.auth.dto.LoginRequest;
import com.rares.budget_management_app.auth.dto.RegisterRequest;
import com.rares.budget_management_app.common.exception.DuplicateResourceException;
import com.rares.budget_management_app.common.exception.Error;
import com.rares.budget_management_app.common.security.JwtUtil;
import com.rares.budget_management_app.user.Role;
import com.rares.budget_management_app.user.User;
import com.rares.budget_management_app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(Error.EMAIL_ALREADY_EXISTS, request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        return new AuthResponse(jwtUtil.generateToken(user));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        return new AuthResponse(jwtUtil.generateToken(user));
    }
}
