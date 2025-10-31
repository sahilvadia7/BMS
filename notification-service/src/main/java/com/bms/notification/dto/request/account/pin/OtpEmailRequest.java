package com.bms.notification.dto.request.account.pin;

import lombok.Data;

@Data
public class OtpEmailRequest {
    private String cifNumber;
    private String email;
    private String otp;
}
