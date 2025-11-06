package com.bms.notification.dto.request.account;

import lombok.Data;

@Data
public class AccountCreationNotificationRequest {
    private String customerName;
    private String email;
    private String cifNumber;
    private String accountNumber;
    private String accountType;
//    private String accountPin;
    private String accountPin;
}

