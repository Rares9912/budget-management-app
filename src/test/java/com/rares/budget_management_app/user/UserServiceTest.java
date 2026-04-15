package com.rares.budget_management_app.user;

import com.rares.budget_management_app.common.exception.DuplicateResourceException;
import com.rares.budget_management_app.user.dto.UserProfileRequest;
import com.rares.budget_management_app.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1)
                .email("john@test.com")
                .name("John")
                .password("encoded_old")
                .role(Role.USER)
                .build();

    }

    @Test
    void updateProfile_updatesEmail_whenNewEmailIsAvailable() {
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse result = userService.updateProfile(user, new UserProfileRequest("new@test.com", null, null));

        assertThat(result.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void updateProfile_doesNotCheckUniqueness_whenEmailIsUnchanged() {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse result = userService.updateProfile(user, new UserProfileRequest("john@test.com", null, null));

        assertThat(result.getEmail()).isEqualTo("john@test.com");
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void updateProfile_throwsDuplicateResourceException_whenEmailAlreadyTaken() {
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateProfile(user, new UserProfileRequest("taken@test.com", null, null)))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_updatesName() {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse result = userService.updateProfile(user, new UserProfileRequest(null, "Jane", null));

        assertThat(result.getName()).isEqualTo("Jane");
    }

    @Test
    void updateProfile_encodesAndUpdatesPassword() {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode("newpass")).thenReturn("encoded_new");

        userService.updateProfile(user, new UserProfileRequest(null, null, "newpass"));

        verify(passwordEncoder).encode("newpass");
        assertThat(user.getPassword()).isEqualTo("encoded_new");
    }

    @Test
    void updateProfile_doesNotModifyFields_whenRequestIsEmpty() {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        userService.updateProfile(user, new UserProfileRequest(null, null, null));

        assertThat(user.getEmail()).isEqualTo("john@test.com");
        assertThat(user.getName()).isEqualTo("John");
        verify(passwordEncoder, never()).encode(any());
    }
}