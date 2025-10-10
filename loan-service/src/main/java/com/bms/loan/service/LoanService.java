package com.bms.loan.service;

import com.bms.loan.dto.request.ApproveLoanRequestDto;
import com.bms.loan.dto.request.DisburseLoanRequestDto;
import com.bms.loan.dto.request.LoanApplicationDto;
import com.bms.loan.dto.request.RepaymentRequestDto;
import com.bms.loan.dto.response.*;

import java.math.BigDecimal;
import java.util.List;

public interface LoanService {
    LoanApplicationResponse applyLoan(LoanApplicationDto dto);

    LoanEvaluationResponse evaluateLoan(Long loanId, Integer creditScore, BigDecimal monthlyIncome, BigDecimal existingEmi);

    LoanApprovalResponse approveLoan(Long loanId, ApproveLoanRequestDto dto);

    LoanRejectionResponseDto rejectLoan(Long loanId, String reason);

    LoanDisbursementResponseDto disburseLoan(Long loanId, DisburseLoanRequestDto dto);

    List<?> getRepaymentSchedule(Long loanId);

    RepaymentResponseDto repayLoan(Long loanId, RepaymentRequestDto dto);

    void markOverdue(); // scheduled job

    LoanCloseResponseDto closeLoan(Long loanId);
}
