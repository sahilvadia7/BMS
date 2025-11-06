package com.bms.creditscore.dto.extrenal;

import lombok.Data;

@Data
public class AccountSummaryDTO {
    private Long customerId;
    private double totalCreditLimit; // sum of card limits, lines
    private double totalUsedCredit;  // current usage across cards/lines
}
