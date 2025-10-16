package com.bms.customer.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutRequest {

    @NotBlank(message = "CIF number is required")
    private String cifNumber;

    @NotBlank(message = "Token is required")
    private String token;

    private String deviceInfo;
}

