package com.bms.customer.services.impl;

import com.bms.customer.dtos.resetpass.OtpEmailDTO;
import com.bms.customer.dtos.resetpass.OtpRequestDTO;
import com.bms.customer.dtos.resetpass.OtpVerifyDTO;
import com.bms.customer.dtos.resetpass.PasswordResetDTO;
import com.bms.customer.entities.Customer;
import com.bms.customer.entities.CustomerOtp;
import com.bms.customer.exception.OtpValidationException;
import com.bms.customer.exception.ResourceNotFoundException;
import com.bms.customer.feign.NotificationClient;
import com.bms.customer.repositories.CustomerOtpRepository;
import com.bms.customer.repositories.CustomerRepository;
import com.bms.customer.services.ForgotPasswordService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@Slf4j
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private final CustomerRepository customerRepository;
    private final CustomerOtpRepository customerOtpRepository;
    private final NotificationClient notificationClient;
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordServiceImpl(CustomerRepository customerRepository,
            CustomerOtpRepository customerOtpRepository,
            NotificationClient notificationClient,
            PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.customerOtpRepository = customerOtpRepository;
        this.notificationClient = notificationClient;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void requestOtp(OtpRequestDTO request) {
        Customer customer = customerRepository
                .findByCifNumberAndEmail(request.getCifId(), request.getEmail())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Customer not found with the provided CIF ID and email."));

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        CustomerOtp otpEntity = CustomerOtp.builder()
                .customer(customer)
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .verified(false)
                .createdAt(LocalDateTime.now())
                .attempts(0)
                .build();

        customerOtpRepository.save(otpEntity);

        OtpEmailDTO emailDTO = OtpEmailDTO.builder()
                .toEmail(customer.getEmail())
                .customerName(customer.getFirstName())
                .otp(otp)
                .build();

        try {
            notificationClient.sendOtpEmail(emailDTO);
        } catch (Exception e) {
            log.error("Failed to send OTP email via notification service for CIF: {}. Reason: {}", request.getCifId(),
                    e.getMessage());
        }
    }

    @Override
    public void verifyOtp(OtpVerifyDTO request) {
        Customer customer = customerRepository.findByCifNumber(request.getCifId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid CIF ID."));

        CustomerOtp otpEntity = customerOtpRepository.findTopByCustomerOrderByCreatedAtDesc(customer)
                .orElseThrow(() -> new ResourceNotFoundException("No OTP has been requested for this customer."));

        if (otpEntity.isUsed()) {
            throw new OtpValidationException("This OTP has already been used.");
        }
        if (otpEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OtpValidationException("OTP has expired. Please request a new one.");
        }
        if (!otpEntity.getOtp().equals(request.getOtp())) {
            throw new OtpValidationException("The OTP provided is invalid.");
        }

        otpEntity.setVerified(true);
        customerOtpRepository.save(otpEntity);
    }

    @Override
    public void resetPassword(PasswordResetDTO request) {
        Customer customer = customerRepository.findByCifNumber(request.getCifId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid CIF ID."));

        CustomerOtp otpEntity = customerOtpRepository.findTopByCustomerOrderByCreatedAtDesc(customer)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No valid OTP found. Please request and verify a new OTP first."));

        if (!otpEntity.isVerified()) {
            throw new OtpValidationException("OTP has not been verified. Please complete the verification step first.");
        }
        if (otpEntity.isUsed()) {
            throw new OtpValidationException("This OTP has already been used for a password reset.");
        }
        if (otpEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OtpValidationException("OTP has expired. Please request a new one.");
        }

        customer.setPassword(passwordEncoder.encode(request.getNewPassword()));
        customerRepository.save(customer);

        otpEntity.setUsed(true);
        customerOtpRepository.save(otpEntity);
    }
}