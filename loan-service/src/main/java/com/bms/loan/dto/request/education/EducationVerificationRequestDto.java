package com.bms.loan.dto.request.education;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationVerificationRequestDto {
    private boolean admissionVerified;
    private boolean collegeRecognized;
    private boolean feeStructureVerified;
    private boolean studentBackgroundClear;

    private boolean coApplicantIncomeVerified;
    private boolean coApplicantIdentityValid;
}
