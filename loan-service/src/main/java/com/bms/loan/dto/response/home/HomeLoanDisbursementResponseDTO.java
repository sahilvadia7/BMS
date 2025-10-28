package com.bms.loan.dto.response.home;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;


// DTO returned after successful loan disbursement.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeLoanDisbursementResponseDTO {

    private Long loanId;
    private BigDecimal disbursedAmount; // Amount transferred
    private LocalDate disbursementDate; // Date of disbursement
    private String paymentMode;         // NEFT / RTGS / DD
    private String transactionRefNo;    // Bank transaction reference
    private String remarks;             // Optional note
}
