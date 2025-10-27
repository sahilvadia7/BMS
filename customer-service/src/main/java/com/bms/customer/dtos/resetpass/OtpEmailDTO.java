package com.bms.customer.dtos.resetpass;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpEmailDTO {
    private String toEmail;
    private String customerName;
    private String otp;
}