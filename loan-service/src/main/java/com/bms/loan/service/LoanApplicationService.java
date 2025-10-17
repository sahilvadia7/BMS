package com.bms.loan.service;

import com.bms.loan.dto.request.CarLoanEvaluationRequestDto;
import com.bms.loan.dto.request.LoanApplicationRequest;
import com.bms.loan.dto.response.CarLoanEvaluationByBankResponse;
import com.bms.loan.dto.response.LoanApplicationResponse;
import com.bms.loan.dto.response.LoanDisbursementResponse;
import com.bms.loan.dto.response.LoanEvaluationResponse;

public interface LoanApplicationService {
    LoanApplicationResponse applyLoan(LoanApplicationRequest request);

    CarLoanEvaluationByBankResponse updateEvaluationData(Long loanId, CarLoanEvaluationRequestDto request);

    LoanEvaluationResponse evaluateLoan(Long loanId);

    LoanDisbursementResponse disburseLoan(Long loanId);
}
