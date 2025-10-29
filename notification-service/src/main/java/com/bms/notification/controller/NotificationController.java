package com.bms.notification.controller;

import com.bms.notification.dto.OtpEmailDTO;
import com.bms.notification.dto.request.loan.ApplyLoanEmailDTO;
import com.bms.notification.dto.request.loan.DisbursementEmailDTO;
import com.bms.notification.dto.request.EmailRequestDTO;
import com.bms.notification.dto.request.loan.SanctionEmailDTO;
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

    @PostMapping("/send-sanction")
    public ResponseEntity<String> sendSanctionEmail(@RequestBody SanctionEmailDTO request) {
        emailService.sendSanctionLetterEmail(request);
        return ResponseEntity.ok("sanction latter email sent");
    }

    @PostMapping("/send-disbursement")
    public ResponseEntity<String> sendDisbursementEmail(@RequestBody DisbursementEmailDTO request) {
        emailService.sendDisbursementEmail(request);
        return ResponseEntity.ok("disbursement email sent");
    }


    @PostMapping("/send-applyLoan")
    public ResponseEntity<String> sendApplyLoanEmail(@RequestBody ApplyLoanEmailDTO request) {
        emailService.sendApplyLoanEmail(request);
        return ResponseEntity.ok("loan apply details sent");
    }

}

