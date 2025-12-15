package com.bms.loan.dto.request;

import com.bms.loan.dto.request.car.CarLoanVerificationRequestDto;
import com.bms.loan.dto.request.education.EducationVerificationRequestDto;
import com.bms.loan.dto.request.home.HomeLoanVerificationRequestDTO;
import lombok.*;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyLoanRequestDto {
    private String officerName;
    private String officerRemarks;
    private LocalDate verifiedDate;
    private boolean addressVerified;        // common verification item
    private boolean identityVerified;       // KYC cross-check
    private boolean verifiedSuccessfully;   // final status

    private CarLoanVerificationRequestDto carLoanVerificationRequestDto;
    private HomeLoanVerificationRequestDTO homeLoanVerificationRequestDTO;
    private EducationVerificationRequestDto educationVerificationRequestDto;

}
