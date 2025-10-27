package com.bms.loan.dto.request.education;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationLoanDetailsDto {
    private String courseName;
    private String university;
    private Integer courseDurationMonths;
    private BigDecimal tuitionFees;
    private String coApplicantName;
}
