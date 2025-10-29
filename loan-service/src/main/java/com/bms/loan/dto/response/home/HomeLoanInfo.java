package com.bms.loan.dto.response.home;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeLoanInfo {

    private String propertyAddress;
    private BigDecimal propertyValue;
    private String builderName;
    private BigDecimal downPayment;
    private String propertyType;
    private BigDecimal loanToValueRatio;
    private String ownershipType;
    private String registrationNumber;
    private boolean approvedByAuthority;
}
