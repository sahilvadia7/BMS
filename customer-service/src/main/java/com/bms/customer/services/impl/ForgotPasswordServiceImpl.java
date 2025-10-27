package com.bms.customer.services.impl;

import com.bms.customer.dtos.resetpass.OtpEmailDTO;
import com.bms.customer.dtos.resetpass.OtpRequestDTO;
import com.bms.customer.dtos.resetpass.OtpVerifyDTO;
import com.bms.customer.dtos.resetpass.PasswordResetDTO;
import com.bms.customer.entities.Customer;
import com.bms.customer.entities.CustomerOtp;
import com.bms.customer.feign.NotificationClient;
import com.bms.customer.repositories.CustomerOtpRepository;
import com.bms.customer.repositories.CustomerRepository;
import com.bms.customer.services.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private final CustomerRepository customerRepository;
    private final CustomerOtpRepository customerOtpRepository;
    private final NotificationClient notificationClient;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void requestOtp(OtpRequestDTO request) {
        Customer customer = customerRepository
                .findByCifNumberAndEmail(request.getCifId(), request.getEmail())
                .orElseThrow(() -> new RuntimeException("Customer not found with given CIF ID and email."));

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

            notificationClient.sendOtpEmail(emailDTO);



    }

    @Override
    public void verifyOtp(OtpVerifyDTO request) {
        Customer customer = customerRepository.findByCifNumber(request.getCifId())
                .orElseThrow(() -> new RuntimeException("Invalid CIF ID."));

        CustomerOtp otpEntity = customerOtpRepository.findTopByCustomerOrderByCreatedAtDesc(customer)
                .orElseThrow(() -> new RuntimeException("No OTP found for this customer."));

        if (otpEntity.isUsed() || otpEntity.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("OTP expired or already used.");

        if (!otpEntity.getOtp().equals(request.getOtp()))
            throw new RuntimeException("Invalid OTP.");

        otpEntity.setVerified(true);
        customerOtpRepository.save(otpEntity);
    }

    @Override
    public void resetPassword(PasswordResetDTO request) {
        Customer customer = customerRepository.findByCifNumber(request.getCifId())
                .orElseThrow(() -> new RuntimeException("Invalid CIF ID."));

        CustomerOtp otpEntity = customerOtpRepository.findTopByCustomerOrderByCreatedAtDesc(customer)
                .orElseThrow(() -> new RuntimeException("No OTP found for this customer."));

        if (!otpEntity.isVerified())
            throw new RuntimeException("OTP not verified. Please verify your OTP before resetting password.");

        if (otpEntity.isUsed())
            throw new RuntimeException("OTP already used for password reset.");

        if (otpEntity.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("OTP expired. Please request a new one.");

        customer.setPassword(passwordEncoder.encode(request.getNewPassword()));
        customerRepository.save(customer);

        otpEntity.setUsed(true);
        customerOtpRepository.save(otpEntity);
    }
}
