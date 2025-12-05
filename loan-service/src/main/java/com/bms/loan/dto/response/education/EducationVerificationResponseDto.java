package com.bms.loan.dto.response.education;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationVerificationResponseDto {
    private boolean admissionVerified;
    private boolean collegeRecognized;
    private boolean feeStructureVerified;
    private boolean studentBackgroundClear;

    private boolean coApplicantIncomeVerified;
    private boolean coApplicantIdentityValid;

    private String officerName;
    private String officerRemarks;
    private LocalDate visitDate;
    private boolean verifiedSuccessfully;
}
