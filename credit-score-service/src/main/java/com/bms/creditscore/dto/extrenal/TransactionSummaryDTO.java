package com.bms.creditscore.dto.extrenal;

import lombok.Data;

@Data
public class TransactionSummaryDTO {
    private Long customerId;
    private int onTimePayments;   // count in last 12 months
    private int totalPayments;    // count in last 12 months
    private int recentInquiries;  // count in last 3-6 months (if available)
    private int accountAgeYears;  // years since first credit account (if available)
}
