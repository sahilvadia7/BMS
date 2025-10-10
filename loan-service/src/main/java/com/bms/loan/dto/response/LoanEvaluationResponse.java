package com.bms.loan.dto.response;

import com.bms.loan.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanEvaluationResponse {
    private Long loanId;
    private LoanStatus status;
    private Integer creditScore;
    private BigDecimal principal;
    private BigDecimal tenureMonths;
    private BigDecimal annualRate;
    private BigDecimal calculatedEmi;
    private BigDecimal maxEligibleEmi;
    private String message;
}
