package com.bms.account.services.impl;

import com.bms.account.dtos.CustomerResponseDTO;
import com.bms.account.dtos.accountPin.OtpEmailRequest;
import com.bms.account.dtos.accountPin.OtpVerificationDTO;
import com.bms.account.dtos.accountPin.PinResetDTO;
import com.bms.account.dtos.accountPin.PinResetRequestDTO;
import com.bms.account.entities.Account;
import com.bms.account.entities.AccountOtp;
import com.bms.account.feign.CustomerClient;
import com.bms.account.feign.NotificationClient;
import com.bms.account.repositories.AccountOtpRepository;
import com.bms.account.repositories.AccountRepository;
import com.bms.account.services.AccountPinResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountPinResetServiceImpl implements AccountPinResetService {

    private final AccountRepository accountRepository;
    private final AccountOtpRepository otpRepository;
    private final NotificationClient notificationClient;
    private final CustomerClient customerClient;

    // 1️ Request OTP
    @Override
    public String requestOtp(PinResetRequestDTO request) {
        // Find customer details via CIF
        CustomerResponseDTO customer = customerClient.getByCif(request.getCifNumber());
        if (customer == null) {
            return "Customer not found for CIF: " + request.getCifNumber();
        }

        // Generate OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        AccountOtp accountOtp = otpRepository.findByCifNumber(request.getCifNumber())
                .orElse(new AccountOtp());
        accountOtp.setCifNumber(request.getCifNumber());
        accountOtp.setOtp(otp);
        accountOtp.setExpiryTime(expiry);
        otpRepository.save(accountOtp);

        //  Send OTP via email using Notification Service
        OtpEmailRequest otpRequest = new OtpEmailRequest();
        otpRequest.setCifNumber(request.getCifNumber());
        otpRequest.setEmail(customer.getEmail()); // from verified customer service
        otpRequest.setOtp(otp);
        notificationClient.sendOtpEmailPin(otpRequest);

        return "OTP sent to your registered email (" + customer.getEmail() + ")";
    }

    // 2️ Verify OTP
    @Override
    public String verifyOtp(OtpVerificationDTO dto) {
        Optional<AccountOtp> otpOptional = otpRepository.findByCifNumber(dto.getCifNumber());
        if (otpOptional.isEmpty()) return "No OTP request found";

        AccountOtp storedOtp = otpOptional.get();

        if (storedOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            return "OTP expired";
        }

        if (!storedOtp.getOtp().equals(dto.getOtp())) {
            return "Invalid OTP";
        }

        return "OTP verified successfully";
    }

    // 3️ Reset PIN
    @Override
    public String resetPin(PinResetDTO dto) {
        Optional<AccountOtp> otpOptional = otpRepository.findByCifNumber(dto.getCifNumber());
        if (otpOptional.isEmpty()) return "No OTP request found";

        AccountOtp storedOtp = otpOptional.get();

        if (storedOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            return "OTP expired";
        }

        if (!storedOtp.getOtp().equals(dto.getOtp())) {
            return "Invalid OTP";
        }

        // Fetch  accounts for that CIF
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(dto.getAccountNumber());
        if (accountOptional.isEmpty()) {
            return "Account not found for account number: " + dto.getAccountNumber();
        }

        Account account = accountOptional.get();

// Extra security check: verify this account belongs to same CIF
        if (!account.getCifNumber().equals(dto.getCifNumber())) {
            return "CIF number does not match account owner!";
        }

        account.setAccountPin(dto.getNewPin());
        accountRepository.save(account);
        otpRepository.delete(storedOtp);

        return "PIN reset successfully for account: " + dto.getAccountNumber();
    }
}