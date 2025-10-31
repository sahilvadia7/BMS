package com.bms.loan.dto.response.education;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationEvaluationResponse {

    private Long loanId;
    private boolean verifiedSuccessfully;
    private String officerName;
    private String remarks;
    private LocalDate verificationDate;
    private String status;
    private String message;
}
