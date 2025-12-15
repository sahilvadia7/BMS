package com.bms.account.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AccountCloseRequestNotification {
    private String customerName;
    private String email;
    private String accountNumber;
//    private String reason;
}
