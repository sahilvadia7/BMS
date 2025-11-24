package com.bms.loan.dto.request.loan.LoanHistory;

import jakarta.validation.Valid;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanHistoryDetailsDto {

    private boolean haveExistingLoans; // true if any active/closed loans exist

    @Valid
    private List<ActiveLoanDto> activeLoans; // current running loans

    @Valid
    private List<ClosedLoanDto> closedLoans; // past loans, already paid off

    // Summary details (calculated client or backend side)
    private BigDecimal totalOutstandingAmount; // sum of remaining amounts of active loans
    private BigDecimal totalMonthlyEmi; // total of current EMIs (active loans)
    private int totalClosedLoans;  // total number of loans fully paid
    private int totalActiveLoans;  // total ongoing loans
}
