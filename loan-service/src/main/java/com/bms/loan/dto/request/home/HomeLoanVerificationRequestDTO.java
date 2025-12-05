package com.bms.loan.dto.request.home;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeLoanVerificationRequestDTO {

    private boolean ownershipVerified;
    private boolean neighbourCheckDone;
    private boolean propertyConditionOk;

    private BigDecimal evaluatedValue;

    private String propertyType;
    private BigDecimal propertyArea;
}
