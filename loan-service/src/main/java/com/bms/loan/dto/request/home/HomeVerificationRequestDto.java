package com.bms.loan.dto.request.home;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

// DTO for field officer to submit property verification data.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeVerificationRequestDto {

    private Long loanId;              // which loan this verification belongs to
    private boolean addressVerified;
    private boolean ownershipVerified;
    private boolean propertyConditionOk;

    private BigDecimal evaluatedValue;   // Real valuation based on inspection

    private String officerName;
    private String officerRemarks;
    private LocalDate visitDate;

}
