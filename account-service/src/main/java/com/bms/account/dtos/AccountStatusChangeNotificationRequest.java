package com.bms.account.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AccountStatusChangeNotificationRequest {

    private String customerName;
    private String email;
    private String accountNumber;
    private String newStatus;
    private String reason;
}
