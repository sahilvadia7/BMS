package com.bms.loan.dto.response.loan;

import com.bms.loan.dto.response.car.CarLoanInfo;
import com.bms.loan.dto.response.home.HomeLoanInfo;
import com.bms.loan.enums.LoanStatus;
import com.bms.loan.enums.LoanType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDetailsResponse {

    private Long loanId;
    private String cifNumber;
    private LoanType loanType;
    private LoanStatus status;
    private String bankName;
    private String bankAccount;
    private String ifscCode;
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private BigDecimal outstandingAmount;
    private BigDecimal totalAmountPaid;
    private BigDecimal totalInterestPaid;
    private BigDecimal totalLateFee;
    private Integer totalPaidEmiCount;
    private LocalDate disbursementDate;
    private LocalDate nextDueDate;
    private String remarks;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    // Type-specific details
    private HomeLoanInfo homeLoanDetails;
    private CarLoanInfo carLoanDetails;
}
