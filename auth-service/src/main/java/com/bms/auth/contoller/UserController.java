package com.bms.auth.contoller;

import com.bms.auth.dto.request.*;
import com.bms.auth.dto.response.LoginResponse;
import com.bms.auth.dto.response.RegisterResponse;
import com.bms.auth.dto.response.UserResponse;
import com.bms.auth.service.OtpService;
import com.bms.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication Management", description = "Endpoints for user registration, login, and profile management")
public class UserController {

    private final UserService userService;
    private final OtpService otpService;

    public UserController(UserService userService, OtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    @Operation(summary = "Register a new user", description = "Access: Public")
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> createUser(@Valid @RequestBody RegisterRequest userDto) {
        RegisterResponse response = userService.registerUser(userDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Login user", description = "Access: Public")
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest userDto) {
        LoginResponse response = userService.loginUser(userDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout user", description = "Access: Authenticated User")
    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(@RequestBody LogoutRequest request) {
        userService.logout(request.userId());
        return ResponseEntity.ok("User logged out successfully!");
    }

    // GET user profile by ID
    @Operation(summary = "Get user profile by ID", description = "Access: Admin, Authenticated User")
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        UserResponse response = userService.getUser(userId);
        return ResponseEntity.ok(response);
    }

    // PUT update user info
    @Operation(summary = "Update user profile", description = "Access: Authenticated User")
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserDTO userDTO) {
        UserResponse updatedUser = userService.updateUser(userId, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    // PATCH change password
    @Operation(summary = "Change password", description = "Access: Authenticated User")
    @PatchMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePwdDTO dto) {
        userService.changePassword(dto);
        return ResponseEntity.ok("Password changed successfully!");
    }

    @Operation(summary = "Toggle user status (Active/Inactive)", description = "Access: Admin")
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<String> toggleUserStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return ResponseEntity.ok("User status toggled successfully!");
    }
}
