package com.rares.budget_management_app.auth;

import com.rares.budget_management_app.auth.dto.AuthResponse;
import com.rares.budget_management_app.auth.dto.LoginRequest;
import com.rares.budget_management_app.auth.dto.RegisterRequest;
import com.rares.budget_management_app.common.exception.DuplicateResourceException;
import com.rares.budget_management_app.common.security.JwtUtil;
import com.rares.budget_management_app.user.Role;
import com.rares.budget_management_app.user.User;
import com.rares.budget_management_app.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_savesUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("John", "john@test.com", "password");

        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(jwtUtil.generateToken(any(User.class))).thenReturn("token123");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("token123");
        verify(userRepository).save(argThat(u ->
                u.getEmail().equals("john@test.com") &&
                u.getPassword().equals("encoded") &&
                u.getRole() == Role.USER
        ));
    }

    @Test
    void register_throwsDuplicateResourceException_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("John", "john@test.com", "password");

        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class);

        // verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsToken_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("john@test.com", "password");
        User user = buildUser();

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user)).thenReturn("token123");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("token123");
    }

    @Test
    void login_throwsBadCredentialsException_whenPasswordIsWrong() {
        LoginRequest request = new LoginRequest("john@test.com", "wrong");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        // verify(jwtUtil, never()).generateToken(any());
    }

    private User buildUser() {
        return User.builder()
                .id(1)
                .email("john@test.com")
                .password("encoded")
                .name("John")
                .role(Role.USER)
                .build();
    }
}