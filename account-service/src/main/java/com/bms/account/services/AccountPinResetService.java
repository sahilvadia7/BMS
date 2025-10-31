package com.bms.account.services;

import com.bms.account.dtos.accountPin.OtpVerificationDTO;
import com.bms.account.dtos.accountPin.PinResetDTO;
import com.bms.account.dtos.accountPin.PinResetRequestDTO;

public interface AccountPinResetService {
    String requestOtp(PinResetRequestDTO request);

    String verifyOtp(OtpVerificationDTO dto);

    String resetPin(PinResetDTO dto);
}
