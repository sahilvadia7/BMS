package com.bms.auth.contoller;

import com.bms.auth.dto.request.*;
import com.bms.auth.dto.response.LoginResponse;
import com.bms.auth.dto.response.RegisterResponse;
import com.bms.auth.dto.response.UserResponse;
import com.bms.auth.service.OtpService;
import com.bms.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class UserController {

    private final UserService userService;
    private final OtpService otpService;

    public UserController(UserService userService, OtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> createUser(@Valid @RequestBody RegisterRequest userDto) {
        RegisterResponse response = userService.registerUser(userDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest userDto) {
        LoginResponse response = userService.loginUser(userDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(@RequestBody LogoutRequest request) {
        userService.logout(request.userId());
        return ResponseEntity.ok("User logged out successfully!");
    }

    // GET user profile by ID
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        UserResponse response = userService.getUser(userId);
        return ResponseEntity.ok(response);
    }

    // PUT update user info
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserDTO userDTO
    ) {
        UserResponse updatedUser = userService.updateUser(userId, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    // PATCH change password
    @PatchMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePwdDTO dto) {
        userService.changePassword(dto);
        return ResponseEntity.ok("Password changed successfully!");
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<String> toggleUserStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return ResponseEntity.ok("User status toggled successfully!");
    }
}
