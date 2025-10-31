package com.bms.account.dtos.accountPin;

import lombok.Data;

@Data
public class OtpEmailRequest {
    private String cifNumber;
    private String email;
    private String otp;
}