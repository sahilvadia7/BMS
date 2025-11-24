package com.bms.loan.dto.request.loan.LoanHistory;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClosedLoanDto {
    private String loanType;
    private BigDecimal loanAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String bankOrLenderName;
    private boolean closedOnTime; // true if repaid without delay
    private String closureReason; // "Completed", "Settled"
}
