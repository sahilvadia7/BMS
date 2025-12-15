package com.bms.loan.dto.response;

import com.bms.loan.dto.response.car.CarVerificationResponseDto;
import com.bms.loan.dto.response.education.EducationVerificationResponseDto;
import com.bms.loan.dto.response.home.HomeVerificationResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationResponseDto {
    private String loanType;

    private CarVerificationResponseDto carReport;
    private HomeVerificationResponseDto homeReport;
    private EducationVerificationResponseDto educationReport;
}
