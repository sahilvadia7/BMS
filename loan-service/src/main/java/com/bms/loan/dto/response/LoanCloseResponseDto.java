package com.bms.loan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanCloseResponseDto {
    private Long loanId;
    private Long customerId;
    private String accountNumber;
    private String productCode; // e.g., "CAR", "HOME"
    private String status;      // e.g., "CLOSED"
    private BigDecimal outstandingBalance;
    private String message;     // e.g., "Loan closed successfully"
}
