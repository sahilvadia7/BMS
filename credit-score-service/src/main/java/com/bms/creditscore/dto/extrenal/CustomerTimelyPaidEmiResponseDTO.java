package com.bms.creditscore.dto.extrenal;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerTimelyPaidEmiResponseDTO {
    private int totalLoans;
    private int totalEmis;
    private int timelyPaidEmis;
    private int lateOrMissedEmis;
    private BigDecimal totalEmiAmount;
    private BigDecimal totalPaidAmount;
    private BigDecimal pendingAmount;
    private List<LoanWiseEmiDetailsDTO> loanWiseDetails;
}
