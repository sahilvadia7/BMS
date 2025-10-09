package com.bms.auth.contoller;

import com.bms.auth.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/send")
    public ResponseEntity<String> sendOtp(@RequestParam String mobileNo) {
        otpService.generateOtp(mobileNo);
        return ResponseEntity.ok("OTP sent successfully to " + mobileNo);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(@RequestParam String mobileNo, @RequestParam String otp) {
        boolean valid = otpService.verifyOtp(mobileNo, otp);
        return valid ? ResponseEntity.ok("OTP verified successfully!") :
                ResponseEntity.status(400).body("Invalid or expired OTP");
    }

}
