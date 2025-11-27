package com.bms.account.controller;

import com.bms.account.dtos.accountPin.OtpVerificationDTO;
import com.bms.account.dtos.accountPin.PinResetDTO;
import com.bms.account.dtos.accountPin.PinResetRequestDTO;
import com.bms.account.services.AccountPinResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts/pin")
@Tag(name = "Account PIN Reset", description = "Operations for resetting account PIN")
public class AccountPinResetController {

    private final AccountPinResetService pinResetService;

    public AccountPinResetController(AccountPinResetService pinResetService) {
        this.pinResetService = pinResetService;
    }

    @Operation(summary = "Request OTP for PIN reset", description = "Access: Customer")
    @PostMapping("/request-reset")
    public ResponseEntity<String> requestOtp(@RequestBody PinResetRequestDTO request) {
        return ResponseEntity.ok(pinResetService.requestOtp(request));
    }

    @Operation(summary = "Verify OTP for PIN reset", description = "Access: Customer")
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpVerificationDTO dto) {
        return ResponseEntity.ok(pinResetService.verifyOtp(dto));
    }

    @Operation(summary = "Reset PIN", description = "Access: Customer")
    @PostMapping("/reset")
    public ResponseEntity<String> resetPin(@RequestBody PinResetDTO dto) {
        return ResponseEntity.ok(pinResetService.resetPin(dto));
    }
}
