package com.bms.loan.dto.response.emi;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanWiseEmiDetailsDTO {
    private Long loanId;
    private int totalEmi;
    private int timelyPaid;
    private int latePaid;
    private BigDecimal emiTotalAmount;
    private BigDecimal paidAmount;
    private BigDecimal currentEmi;
    private LocalDate currentEmiDueDate;
    private String currentEmiStatus;
}
