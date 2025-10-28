package com.bms.loan.dto.response.home;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

// DTO returned after verification is saved successfully.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeVerificationResponse {
    private Long loanId;
    private boolean verifiedSuccessfully;
    private BigDecimal evaluatedValue;
    private String officerName;
    private String remarks;
    private LocalDate verificationDate;
    private String message;
    private String status;
}
