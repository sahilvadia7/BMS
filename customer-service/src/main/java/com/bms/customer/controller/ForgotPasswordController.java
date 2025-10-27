package com.bms.customer.controller;

import com.bms.customer.dtos.resetpass.OtpRequestDTO;
import com.bms.customer.dtos.resetpass.OtpVerifyDTO;
import com.bms.customer.dtos.resetpass.PasswordResetDTO;
import com.bms.customer.services.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/forgot-password")
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    @PostMapping("/request-otp")
    public ResponseEntity<String> requestOtp(@RequestBody OtpRequestDTO request) {
        forgotPasswordService.requestOtp(request);
        return ResponseEntity.ok("OTP sent to your registered email.");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpVerifyDTO request) {
        forgotPasswordService.verifyOtp(request);
        return ResponseEntity.ok("OTP verified successfully.");
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetDTO request) {
        forgotPasswordService.resetPassword(request);
        return ResponseEntity.ok("Password reset successfully.");
    }
}
