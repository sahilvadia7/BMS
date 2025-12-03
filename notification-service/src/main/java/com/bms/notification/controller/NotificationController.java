package com.bms.notification.controller;

import com.bms.notification.dto.OtpEmailDTO;
import com.bms.notification.dto.Transaction;
import com.bms.notification.dto.request.account.AccountCreationNotificationRequest;
import com.bms.notification.dto.request.account.pin.OtpEmailRequest;
import com.bms.notification.dto.request.loan.ApplyLoanEmailDTO;
import com.bms.notification.dto.request.loan.DisbursementEmailDTO;
import com.bms.notification.dto.request.EmailRequestDTO;
import com.bms.notification.dto.request.loan.SanctionEmailDTO;
import com.bms.notification.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification Management", description = "Endpoints for sending emails and notifications")
public class NotificationController {

    private final EmailService emailService;

    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping
    public String greet() {
        return "hello world";
    }

    @Operation(summary = "Send registration email", description = "Access: Internal")
    @PostMapping("/send-registration-email")
    public ResponseEntity<String> sendRegistrationEmail(@RequestBody EmailRequestDTO request) {
        emailService.sendRegistrationEmail(request);
        return ResponseEntity.ok("Email sent successfully");
    }

    @Operation(summary = "Send OTP email", description = "Access: Internal")
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtpEmail(@RequestBody OtpEmailDTO request) {
        emailService.sendOtpEmail(request);
        return ResponseEntity.ok("OTP email sent");
    }

    @Operation(summary = "Send sanction letter email", description = "Access: Internal")
    @PostMapping("/send-sanction")
    public ResponseEntity<String> sendSanctionEmail(@RequestBody SanctionEmailDTO request) {
        emailService.sendSanctionLetterEmail(request);
        return ResponseEntity.ok("sanction latter email sent");
    }

    @Operation(summary = "Send disbursement email", description = "Access: Internal")
    @PostMapping("/send-disbursement")
    public ResponseEntity<String> sendDisbursementEmail(@RequestBody DisbursementEmailDTO request) {
        emailService.sendDisbursementEmail(request);
        return ResponseEntity.ok("disbursement email sent");
    }

    @Operation(summary = "Send loan application email", description = "Access: Internal")
    @PostMapping("/send-applyLoan")
    public ResponseEntity<String> sendApplyLoanEmail(@RequestBody ApplyLoanEmailDTO request) {
        emailService.sendApplyLoanEmail(request);
        return ResponseEntity.ok("loan apply details sent");
    }

    @Operation(summary = "Send account creation notification", description = "Access: Internal")
    @PostMapping("/account-created")
    public void sendAccountCreationNotification(@RequestBody AccountCreationNotificationRequest request) {
        emailService.sendAccountCreatedEmail(request);
    }

    @Operation(summary = "Send OTP email for PIN", description = "Access: Internal")
    @PostMapping("/send-pin-otp")
    public ResponseEntity<String> sendOtpEmail(@RequestBody OtpEmailRequest request) {
        emailService.sendOtpEmailPin(request);
        return ResponseEntity.ok("OTP email sent successfully");
    }

    @Operation(summary = "Send transaction statement", description = "Access: Internal")
    @PostMapping(value = "/send-transaction-statement", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void sendStatement(
            @RequestPart("accountNumber") String accountNumber,
            @RequestPart("name") String name,
            @RequestPart("toEmail") String toEmail,
            @RequestPart("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new RuntimeException("PDF file is missing.");
        }

        byte[] pdfBytes = file.getBytes();

        emailService.downloadTransactionStatement(
                accountNumber,
                name,
                toEmail,
                pdfBytes);
    }

    @Operation(summary = "Send Alert for Transaction ", description = "Access: Internal")
    @PostMapping("/transaction-alert")
    public void sendTransactionAlert(@RequestBody Transaction request,
                                                       @RequestParam String email ) {
        emailService.sendTransactionAlert(request,email);
    }
}
