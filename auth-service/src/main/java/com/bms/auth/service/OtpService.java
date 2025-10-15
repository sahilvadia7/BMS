package com.bms.auth.service;

import com.bms.auth.dto.request.MailRequest;

public interface OtpService {
//    public void generateOtp(String mobileNo);

//    public boolean verifyOtp(String mobileNo, String otp);

    public String generateOtp();

    public void sendOtp(MailRequest mailRequest);

    public String validateOtp(String email, String enteredOtp);

    }
