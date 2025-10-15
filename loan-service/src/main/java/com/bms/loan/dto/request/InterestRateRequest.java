package com.bms.loan.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestRateRequest {

    private String loanType;

    private double baseRate;

    private double maxLTV;

    private int minTenure;

    private int maxTenure;
}
