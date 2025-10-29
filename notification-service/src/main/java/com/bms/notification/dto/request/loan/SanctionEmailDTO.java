package com.bms.notification.dto.request.loan;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SanctionEmailDTO {
    private String toEmail;
    private String customerName;
    private String loanType;
    private BigDecimal sanctionedAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private BigDecimal emiAmount;
    private LocalDate sanctionDate;
}
