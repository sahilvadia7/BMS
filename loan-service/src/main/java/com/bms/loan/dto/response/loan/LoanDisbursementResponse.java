package com.bms.loan.dto.response.loan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDisbursementResponse {
    private Long loanId;
    private String status;        // DISBURSED
    private BigDecimal emi;       // Calculated EMI per month
    private String message;       // Success message
}
