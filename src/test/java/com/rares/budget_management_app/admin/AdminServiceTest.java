package com.rares.budget_management_app.admin;

import com.rares.budget_management_app.admin.dto.AdminCreateUserRequest;
import com.rares.budget_management_app.admin.dto.AdminUpdateUserRequest;
import com.rares.budget_management_app.budget.BudgetService;
import com.rares.budget_management_app.category.CategoryService;
import com.rares.budget_management_app.common.exception.DuplicateResourceException;
import com.rares.budget_management_app.common.exception.ResourceNotFoundException;
import com.rares.budget_management_app.expense.ExpenseService;
import com.rares.budget_management_app.user.Role;
import com.rares.budget_management_app.user.User;
import com.rares.budget_management_app.user.UserRepository;
import com.rares.budget_management_app.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private CategoryService categoryService;

    @InjectMocks
    private AdminService adminService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id(1)
                .email("john@test.com")
                .name("John")
                .password("encoded")
                .role(Role.USER)
                .build();
    }

    @Test
    void getAllUsers_returnsListOfUserResponses() {
        when(userRepository.findAll()).thenReturn(List.of(existingUser));

        List<UserResponse> result = adminService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("john@test.com");
    }

    @Test
    void getUser_returnsUserResponse_whenUserExists() {
        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));

        UserResponse result = adminService.getUser(1);

        assertThat(result.getEmail()).isEqualTo("john@test.com");
    }

    @Test
    void getUser_throwsResourceNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.getUser(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createUser_savesUserWithDefaultRoleUser_whenRoleNotSpecified() {
        AdminCreateUserRequest request = new AdminCreateUserRequest("new@test.com", "New User", "pass", null);

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse result = adminService.createUser(request);

        assertThat(result.getEmail()).isEqualTo("new@test.com");
        assertThat(result.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void createUser_savesUserWithAdminRole_whenRoleSpecified() {
        AdminCreateUserRequest request = new AdminCreateUserRequest("admin@test.com", "Admin", "pass", Role.ADMIN);

        when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse result = adminService.createUser(request);

        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void createUser_throwsDuplicateResourceException_whenEmailAlreadyExists() {
        AdminCreateUserRequest request = new AdminCreateUserRequest("john@test.com", "John", "pass", null);

        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

        assertThatThrownBy(() -> adminService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_updatesNameAndRole() {
        AdminUpdateUserRequest request = new AdminUpdateUserRequest(null, "Johnny", null, Role.ADMIN);

        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse result = adminService.updateUser(1, request);

        assertThat(result.getName()).isEqualTo("Johnny");
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void updateUser_encodesPassword_whenPasswordProvided() {
        AdminUpdateUserRequest request = new AdminUpdateUserRequest(null, null, "newpass", null);

        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newpass")).thenReturn("encoded_new");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        adminService.updateUser(1, request);

        verify(passwordEncoder).encode("newpass");
        assertThat(existingUser.getPassword()).isEqualTo("encoded_new");
    }

    @Test
    void updateUser_throwsDuplicateResourceException_whenNewEmailAlreadyTaken() {
        AdminUpdateUserRequest request = new AdminUpdateUserRequest("taken@test.com", null, null, null);

        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> adminService.updateUser(1, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void updateUser_throwsResourceNotFoundException_whenUserNotFound() {
        AdminUpdateUserRequest request = new AdminUpdateUserRequest(null, "Name", null, null);

        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.updateUser(99, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteUser_deletesUser_whenFound() {
        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));

        adminService.deleteUser(1);

        verify(userRepository).delete(existingUser);
    }

    @Test
    void getUserCategories_delegatesToCategoryService_withCorrectUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(categoryService.getAllCategories(existingUser)).thenReturn(List.of());

        adminService.getUserCategories(1);

        verify(categoryService).getAllCategories(existingUser);
    }
}