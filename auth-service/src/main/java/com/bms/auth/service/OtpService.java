package com.bms.auth.service;

public interface OtpService {
    public void generateOtp(String mobileNo);

    public boolean verifyOtp(String mobileNo, String otp);


    }
