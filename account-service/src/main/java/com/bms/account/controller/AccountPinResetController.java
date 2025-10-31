package com.bms.account.controller;

import com.bms.account.dtos.accountPin.OtpVerificationDTO;
import com.bms.account.dtos.accountPin.PinResetDTO;
import com.bms.account.dtos.accountPin.PinResetRequestDTO;
import com.bms.account.services.AccountPinResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts/pin")
@RequiredArgsConstructor
public class AccountPinResetController {

    private final AccountPinResetService pinResetService;

    @PostMapping("/request-reset")
    public ResponseEntity<String> requestOtp(@RequestBody PinResetRequestDTO request) {
        return ResponseEntity.ok(pinResetService.requestOtp(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpVerificationDTO dto) {
        return ResponseEntity.ok(pinResetService.verifyOtp(dto));
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetPin(@RequestBody PinResetDTO dto) {
        return ResponseEntity.ok(pinResetService.resetPin(dto));
    }
}
