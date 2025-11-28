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
import com.bms.account.utility.PinEncoder;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AccountPinResetServiceImpl implements AccountPinResetService {

    private final AccountRepository accountRepository;
    private final AccountOtpRepository otpRepository;
    private final NotificationClient notificationClient;
    private final CustomerClient customerClient;
    private final PinEncoder pinEncoder;

    public AccountPinResetServiceImpl(AccountRepository accountRepository,
            AccountOtpRepository otpRepository,
            NotificationClient notificationClient,
            CustomerClient customerClient,
            PinEncoder pinEncoder) {
        this.accountRepository = accountRepository;
        this.otpRepository = otpRepository;
        this.notificationClient = notificationClient;
        this.customerClient = customerClient;
        this.pinEncoder = pinEncoder;
    }

    @Override
    public String requestOtp(PinResetRequestDTO request) {
        CustomerResponseDTO customer = customerClient.getByCif(request.getCifNumber());
        if (customer == null)
            return "Customer not found for CIF: " + request.getCifNumber();

        String otp = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        AccountOtp accountOtp = otpRepository.findByCifNumber(request.getCifNumber())
                .orElse(new AccountOtp());
        accountOtp.setCifNumber(request.getCifNumber());
        accountOtp.setOtp(otp);
        accountOtp.setExpiryTime(expiry);
        otpRepository.save(accountOtp);

        OtpEmailRequest otpRequest = new OtpEmailRequest();
        otpRequest.setCifNumber(request.getCifNumber());
        otpRequest.setEmail(customer.getEmail());
        otpRequest.setOtp(otp);
        notificationClient.sendOtpEmailPin(otpRequest);

        return "OTP sent to your registered email (" + customer.getEmail() + ")";
    }

    @Override
    public String verifyOtp(OtpVerificationDTO dto) {
        Optional<AccountOtp> otpOpt = otpRepository.findByCifNumber(dto.getCifNumber());
        if (otpOpt.isEmpty())
            return "No OTP request found";

        AccountOtp otp = otpOpt.get();
        if (otp.getExpiryTime().isBefore(LocalDateTime.now()))
            return "OTP expired";
        if (!otp.getOtp().equals(dto.getOtp()))
            return "Invalid OTP";

        return "OTP verified successfully";
    }

    @Override
    public String resetPin(PinResetDTO dto) {
        Optional<AccountOtp> otpOpt = otpRepository.findByCifNumber(dto.getCifNumber());
        if (otpOpt.isEmpty())
            return "No OTP request found";

        AccountOtp otp = otpOpt.get();
        if (otp.getExpiryTime().isBefore(LocalDateTime.now()))
            return "OTP expired";
        if (!otp.getOtp().equals(dto.getOtp()))
            return "Invalid OTP";

        Account account = accountRepository.findByAccountNumber(dto.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account not found for number: " + dto.getAccountNumber()));

        if (!account.getCifNumber().equals(dto.getCifNumber())) {
            return "CIF number does not match account owner!";
        }

        String encodedNewPin = pinEncoder.encode(String.valueOf(dto.getNewPin())); // encode
        account.setAccountPin(encodedNewPin);
        accountRepository.save(account);
        otpRepository.delete(otp);

        return "PIN reset successfully for account: " + dto.getAccountNumber();
    }
}
