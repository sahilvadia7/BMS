package com.bms.customer.services;

import com.bms.customer.dtos.resetpass.OtpRequestDTO;
import com.bms.customer.dtos.resetpass.OtpVerifyDTO;
import com.bms.customer.dtos.resetpass.PasswordResetDTO;

public interface ForgotPasswordService {
    void requestOtp(OtpRequestDTO request);
    void verifyOtp(OtpVerifyDTO request);
    void resetPassword(PasswordResetDTO request);
}
