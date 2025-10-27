package com.bms.notification.controller;

import com.bms.notification.dto.OtpEmailDTO;
import com.bms.notification.dto.request.EmailRequestDTO;
import com.bms.notification.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    @Autowired
    private EmailService emailService;

    @GetMapping
    public String greet(){return "hello world";}

    @PostMapping("/send-registration-email")
    public ResponseEntity<String> sendRegistrationEmail(@RequestBody EmailRequestDTO request) {
        emailService.sendRegistrationEmail(request);
        return ResponseEntity.ok("Email sent successfully");
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtpEmail(@RequestBody OtpEmailDTO request) {
        emailService.sendOtpEmail(request);
        return ResponseEntity.ok("OTP email sent");
    }

}

