package com.bms.loan.dto.email;

import com.bms.loan.dto.response.emi.EmiSummary;
import lombok.* ;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementEmailDTO {
    private String toEmail;
    private String customerName;
    private String loanType;
    private BigDecimal sanctionedAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private BigDecimal emiAmount;
    private LocalDate firstEmiDate;
    private List<EmiSummary> firstFewEmis; // preview (first 3)
}
