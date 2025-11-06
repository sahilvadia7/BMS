package com.bms.creditscore.dto.request;

import lombok.Data;

@Data
public class CustomerRequest {
    private String customerId;
    private double monthlyIncome;
    private int employmentMonths;
    private double averageBankBalance;
    private double utilityPaymentHistory;
}
