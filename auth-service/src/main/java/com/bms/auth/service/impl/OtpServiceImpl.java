package com.bms.auth.service.impl;

import com.bms.auth.service.OtpService;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.twilio.type.PhoneNumber;

import java.util.Map;
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

    @Override
    public void generateOtp(String mobileNo) {
        // ✅ Generate 6-digit OTP
        String otp = String.format("%06d", random.nextInt(999999));

        otpStorage.put(mobileNo, otp);
        expiryStorage.put(mobileNo, System.currentTimeMillis() + OTP_EXPIRY_MS);

        // ✅ Initialize Twilio
        init(accountSid, authToken);

        // ✅ Send SMS
        try {
            Message.creator(
                    new PhoneNumber("+91" + mobileNo),  // Destination (India example)
                    new PhoneNumber(fromPhoneNumber),   // Twilio number
                    "Your OTP for login is: " + otp + " (valid for 1 minute)"
            ).create();

            System.out.println("✅ OTP sent successfully to " + mobileNo);
        } catch (Exception e) {
            System.err.println("❌ Failed to send OTP: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyOtp(String mobileNo, String otp) {
        if (!otpStorage.containsKey(mobileNo)) return false;

        long expiry = expiryStorage.get(mobileNo);
        if (System.currentTimeMillis() > expiry) {
            otpStorage.remove(mobileNo);
            expiryStorage.remove(mobileNo);
            return false;
        }

        boolean isValid = otpStorage.get(mobileNo).equals(otp);
        if (isValid) {
            otpStorage.remove(mobileNo);
            expiryStorage.remove(mobileNo);
        }
        return isValid;    }
}
