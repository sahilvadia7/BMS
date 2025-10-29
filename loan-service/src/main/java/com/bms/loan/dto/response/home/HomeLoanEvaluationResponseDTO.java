package com.bms.loan.dto.response.home;

import lombok.*;

import java.math.BigDecimal;

// DTO returned after evaluating the applicant’s eligibility for a home loan.


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeLoanEvaluationResponseDTO {

    private Long loanId;
    private BigDecimal eligibleAmount;// Max amount applicant is eligible for
    private BigDecimal recommendedTenure; // Recommended tenure (months)
    private BigDecimal interestRate; // Recommended interest rate
    private String evaluationRemarks; // Reasoning or summary of evaluation
    private boolean preApproved;// Flag indicating auto/pre-approval
}
