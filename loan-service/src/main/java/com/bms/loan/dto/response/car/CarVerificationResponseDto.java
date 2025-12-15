package com.bms.loan.dto.response.car;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarVerificationResponseDto {
    private boolean insuranceValid;
    private boolean employmentVerified;
    private boolean carDocumentsVerified;
    private boolean physicalCarInspectionDone;

    private int carConditionScore;
    private int neighbourhoodStabilityScore;
    private int employmentStabilityYears;

    private String officerName;
    private String officerRemarks;
    private LocalDate visitDate;
    private boolean verifiedSuccessfully;
}
