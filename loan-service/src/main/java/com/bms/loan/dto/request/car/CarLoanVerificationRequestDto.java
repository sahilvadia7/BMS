package com.bms.loan.dto.request.car;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarLoanVerificationRequestDto {


    private boolean insuranceValid;
    private boolean employmentVerified;
    private boolean carDocumentsVerified;
    private boolean physicalCarInspectionDone;

    private int carConditionScore;
    private int neighbourhoodStabilityScore;

    private int employmentStabilityYears; // Officer inputs this value
}
