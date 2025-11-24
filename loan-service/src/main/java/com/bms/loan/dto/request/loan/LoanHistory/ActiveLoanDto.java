package com.bms.loan.dto.request.loan.LoanHistory;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveLoanDto {
    private String loanType;  // HOME, CAR
    private BigDecimal loanAmount; // total sanctioned amount
    private int tenureMonths;
    private BigDecimal remainingAmount; // still to repay
    private BigDecimal emiAmount; // current EMI amount
    private LocalDate startDate;
    private LocalDate endDate;
    private String bankOrLenderName;
    private int totalEmis;
    private int timelyPaidEmis;
    private int lateOrMissedEmis;
}
