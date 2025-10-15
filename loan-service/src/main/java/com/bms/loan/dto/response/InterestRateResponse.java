package com.bms.loan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestRateResponse {

    private Long id;

    private String loanType;

    private double baseRate;

    private double maxLTV;

    private int minTenure;

    private int maxTenure;
}
