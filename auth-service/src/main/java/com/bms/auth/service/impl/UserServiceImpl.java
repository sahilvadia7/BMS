package com.bms.auth.service.impl;

import com.bms.auth.dto.request.ChangePwdDTO;
import com.bms.auth.dto.request.LoginRequest;
import com.bms.auth.dto.request.RegisterRequest;
import com.bms.auth.dto.request.UserDTO;
import com.bms.auth.dto.response.LoginResponse;
import com.bms.auth.dto.response.RegisterResponse;
import com.bms.auth.dto.response.UserResponse;
import com.bms.auth.entity.User;
import com.bms.auth.enums.Roles;
import com.bms.auth.exception.customException.InvalidCredentialsException;
import com.bms.auth.exception.customException.ResourceNotFoundException;
import com.bms.auth.repository.UserRepository;
import com.bms.auth.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public RegisterResponse registerUser(RegisterRequest request) {

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Map DTO → Entity
        User user = new User();
        user.setFirstName(request.getFirstName());

        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNo(request.getPhoneNo());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Roles.Customer);
        user.setGender(request.getGender());
        user.setActive(true);

        // Save user
        User savedUser = userRepository.save(user);

        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setUserId(savedUser.getUserId());
        registerResponse.setMessage("User registered successfully! ");
        // Return response DTO
        return registerResponse;
    }

    @Override
    public LoginResponse loginUser(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return LoginResponse.builder()
//                .token(token)
                .email(user.getEmail())
                .role(Roles.valueOf(user.getRoles().name()))
                .status(user.isActive())
                .build();
    }

    @Override
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Toggle the status
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Override
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        return mapToUserResponse(user);    }

    @Override
    public void changePassword(ChangePwdDTO dto) {
        // Find user by email or phone (depends on your login method)
        User user = userRepository.findByPhoneNo(dto.getPhoneNo());

        if (user != null) {
            // Validate current password
            if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }

            // Encode and update new password
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            userRepository.save(user);
        }

    }

    @Override
    public UserResponse updateUser(Long userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        // Update fields if provided
        if (userDTO.getFirstName() != null) user.setFirstName(userDTO.getFirstName());
        if (userDTO.getLastName() != null) user.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) user.setEmail(userDTO.getEmail());
        if (userDTO.getPhoneNo() != null) user.setPhoneNo(userDTO.getPhoneNo());
        if (userDTO.getRoles() != null) user.setRoles(userDTO.getRoles());

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @Override
    public void logout(Long userId) {

        // System.out.println("User logged out: " + userId);

    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhoneNo(user.getPhoneNo());
        response.setIsActive(user.isActive());
        response.setRole(user.getRoles());
        return response;
    }
}
