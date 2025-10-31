package com.bms.account.dtos.accountPin;

import lombok.Data;

@Data
public class OtpVerificationDTO {
    private String cifNumber;
    private String otp;
}
