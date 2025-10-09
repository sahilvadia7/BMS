package com.bms.auth.service;

import com.bms.auth.dto.request.ChangePwdDTO;
import com.bms.auth.dto.request.LoginRequest;
import com.bms.auth.dto.request.RegisterRequest;
import com.bms.auth.dto.request.UserDTO;
import com.bms.auth.dto.response.LoginResponse;
import com.bms.auth.dto.response.RegisterResponse;
import com.bms.auth.dto.response.UserResponse;
import jakarta.validation.Valid;

public interface UserService {
    RegisterResponse registerUser(RegisterRequest request);

    LoginResponse loginUser(LoginRequest request);

    void toggleUserStatus(Long id);

    UserResponse getUser(Long userId);

    void changePassword(@Valid ChangePwdDTO dto);

    UserResponse updateUser(Long userId, @Valid UserDTO userDTO);

    void logout(Long aLong);
}
