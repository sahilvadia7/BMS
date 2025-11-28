package com.bms.auth.service.impl;

import com.bms.auth.dto.request.MailRequest;
import com.bms.auth.entity.OtpVerification;
import com.bms.auth.repository.OtpVerificationRepository;
import com.bms.auth.service.EmailService;
import com.bms.auth.service.OtpService;
import com.twilio.rest.api.v2010.account.Message;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.twilio.type.PhoneNumber;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static com.twilio.Twilio.init;

@Service
public class OtpServiceImpl implements OtpService {

    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    private final Map<String, Long> expiryStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();

    private final long OTP_EXPIRY_MS = 60 * 1000; // 1 minutes

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber; // your Twilio number

    private final OtpVerificationRepository otpRepo;
    private final EmailService emailService;

    public OtpServiceImpl(OtpVerificationRepository otpRepo, EmailService emailService) {
        this.otpRepo = otpRepo;
        this.emailService = emailService;
    }

    @Override
    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // Save and send OTP
    public void sendOtp(MailRequest mailRequest) {
        String otp = generateOtp();

        // Save in DB
        OtpVerification otpEntity = new OtpVerification();
        otpEntity.setEmail(mailRequest.getTo());
        otpEntity.setOtp(otp);
        otpEntity.setCreatedAt(LocalDateTime.now());
        otpRepo.save(otpEntity);

        // Send email
        emailService.sendSimpleEmail(mailRequest.getTo(), mailRequest.getSubject(), "Your verification OTP is: " + otp);
    }

    @Override
    public String validateOtp(String email, String enteredOtp) {

        Optional<OtpVerification> otpRecordOpt = otpRepo.findTopByEmailOrderByCreatedAtDesc(email);

        if (otpRecordOpt.isEmpty()) {
            return "No OTP found for this email.";
        }
        OtpVerification otpRecord = otpRecordOpt.get();

        // Check expiry (1 minute)
        long minutes = ChronoUnit.MINUTES.between(otpRecord.getCreatedAt(), LocalDateTime.now());
        if (minutes >= 1) {
            return "OTP expired. Please request a new one.";
        }

        // Check correctness
        if (!otpRecord.getOtp().equals(enteredOtp)) {
            return "Invalid OTP. Please try again.";
        }

        return "OTP verified successfully!";
    }

    // @Override
    // public void generateOtp(String mobileNo) {
    // // Generate 6-digit OTP
    // String otp = String.format("%06d", random.nextInt(999999));
    //
    // otpStorage.put(mobileNo, otp);
    // expiryStorage.put(mobileNo, System.currentTimeMillis() + OTP_EXPIRY_MS);
    //
    // // Initialize Twilio
    // init(accountSid, authToken);
    //
    // // Send SMS
    // try {
    // Message.creator(
    // new PhoneNumber("+91" + mobileNo), // Destination (India example)
    // new PhoneNumber(fromPhoneNumber), // Twilio number
    // "Your OTP for login is: " + otp + " (valid for 1 minute)"
    // ).create();
    //
    // System.out.println("OTP sent successfully to " + mobileNo);
    // } catch (Exception e) {
    // System.err.println("Failed to send OTP: " + e.getMessage());
    // }
    // }
    // @Override
    // public boolean verifyOtp(String mobileNo, String otp) {
    // if (!otpStorage.containsKey(mobileNo)) return false;
    //
    // long expiry = expiryStorage.get(mobileNo);
    // if (System.currentTimeMillis() > expiry) {
    // otpStorage.remove(mobileNo);
    // expiryStorage.remove(mobileNo);
    // return false;
    // }
    //
    // boolean isValid = otpStorage.get(mobileNo).equals(otp);
    // if (isValid) {
    // otpStorage.remove(mobileNo);
    // expiryStorage.remove(mobileNo);
    // }
    // return isValid;
    // }
}
