package com.bms.loan.dto.response.home;

// DTO returned when a home loan has been sanctioned/approved.

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanSanctionResponseDTO {

    private Long loanId;
    private BigDecimal sanctionedAmount; // Final approved amount
    private BigDecimal interestRate;// Final approved interest
    private Integer tenureMonths;// Final sanctioned tenure
    private LocalDate sanctionDate;// Date of approval
    private String sanctionedBy;// Officer ID/name
    private BigDecimal emiAmount;
    private String remarks;
}
