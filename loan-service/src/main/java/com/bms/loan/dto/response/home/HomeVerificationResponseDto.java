package com.bms.loan.dto.response.home;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeVerificationResponseDto {

    private boolean ownershipVerified;
    private boolean neighbourCheckDone;
    private boolean propertyConditionOk;

    private BigDecimal evaluatedValue;
    private String propertyType;
    private BigDecimal propertyArea;

    private String officerName;
    private String officerRemarks;
    private LocalDate visitDate;
    private boolean verifiedSuccessfully;
}
