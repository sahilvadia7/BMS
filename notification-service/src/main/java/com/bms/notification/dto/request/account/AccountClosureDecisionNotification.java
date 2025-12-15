package com.bms.notification.dto.request.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AccountClosureDecisionNotification {
    private String email;
    private String customerName;
    private String accountNumber;
    private String decision;
    private String reason; // optional (mainly for REJECTED)
}
