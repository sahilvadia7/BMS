package com.bms.account.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreationNotificationRequest {
    private String customerName;
    private String email;
    private String cifNumber;
    private String accountNumber;
    private String accountType;
//    private String accountPin;
    private String accountPin;
}
