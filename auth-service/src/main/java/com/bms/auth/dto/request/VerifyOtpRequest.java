package com.bms.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Invalid mobile number")
    private String mobileNo;

    @NotBlank(message = "OTP is required")
    @Size(min = 4, max = 6, message = "OTP must be 4 to 6 digits")
    private String otp;
}
