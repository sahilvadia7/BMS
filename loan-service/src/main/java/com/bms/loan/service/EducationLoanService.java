package com.bms.loan.service;

import com.bms.loan.dto.response.loan.LoanEvaluationResponse;

public interface EducationLoanService {
    LoanEvaluationResponse evaluateLoan(Long loanId);
}
