package com.bms.creditscore.dto.response;

import lombok.Data;

@Data
public class CustomerResponse {
    private Long id;
    private boolean hasLoans;
    private int creditCards;
    private int onTimePayments;
    private int totalPayments;
    private double usedCredit;
    private double creditLimit;
    private int accountAgeYears;
    private int recentInquiries;
}
