package com.bms.account.dtos.accountPin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PinErrorResponse {
    private String message;
    private int attempts;
    private int attemptsRemaining;
    private boolean locked;
    private Long lockExpiresInSeconds;
}

