package com.bms.loan.dto.response.loan;

import com.bms.loan.dto.request.car.CarLoanDetailsDto;
import com.bms.loan.dto.request.education.EducationLoanDetailsDto;
import com.bms.loan.dto.request.home.HomeLoanDetailsDto;
import com.bms.loan.dto.request.loan.LoanHistory.ActiveLoanDto;
import com.bms.loan.dto.request.loan.LoanHistory.ClosedLoanDto;
import com.bms.loan.dto.response.car.CarLoanInfo;
import com.bms.loan.dto.response.home.HomeLoanInfo;
import com.bms.loan.enums.EmploymentType;
import com.bms.loan.enums.LoanStatus;
import com.bms.loan.enums.LoanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDetailsAdminDto {
    private Long loanId;
    private String cifNumber;
    private LoanType loanType;

    private BigDecimal interestRate;
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private Integer requestedTenureMonths;

    private BigDecimal outstandingAmount;
    private BigDecimal totalAmountPaid;
    private BigDecimal totalInterestPaid;
    private BigDecimal totalLateFee;
    private Integer totalPaidEmiCount;
    private LoanStatus status;

    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    private BigDecimal monthlyIncome;
    private EmploymentType employmentType;

    private String bankName;
    private String bankAccount;
    private String ifscCode;

    private LocalDate disbursementDate;
    private LocalDate nextDueDate;
    private boolean eSign;

    private CarLoanInfo carLoanDetails;
    private HomeLoanInfo homeLoanDetails;
    private LoanSanctionDto loanSanction;

    private List<ActiveLoanDto> activeLoans;
    private List<ClosedLoanDto> closedLoans;
}
