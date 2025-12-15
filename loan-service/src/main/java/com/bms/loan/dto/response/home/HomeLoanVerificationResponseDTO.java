package com.bms.loan.dto.response.home;

import lombok.*;

import java.math.BigDecimal;

// DTO returned after evaluating the applicant’s eligibility for a home loan.


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeLoanVerificationResponseDTO {

    private boolean ownershipVerified;
    private boolean neighbourCheckDone;
    private boolean propertyConditionOk;

    private BigDecimal evaluatedValue;

    private String propertyType;
    private BigDecimal propertyArea;

}
