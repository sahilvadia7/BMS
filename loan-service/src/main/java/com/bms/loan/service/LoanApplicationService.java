package com.bms.loan.service;

import com.bms.loan.dto.request.car.CarLoanEvaluationRequestDto;
import com.bms.loan.dto.request.LoanApplicationRequest;
import com.bms.loan.dto.response.car.CarLoanEvaluationByBankResponse;
import com.bms.loan.dto.response.emi.LoanEmiScheduleResponse;
import com.bms.loan.dto.response.loan.LoanApplicationResponse;
import com.bms.loan.dto.response.loan.LoanDisbursementResponse;
import com.bms.loan.dto.response.loan.LoanEvaluationResponse;

import java.time.LocalDate;
import java.util.List;

public interface LoanApplicationService {
    LoanApplicationResponse applyLoan(LoanApplicationRequest request);

    CarLoanEvaluationByBankResponse updateEvaluationData(Long loanId, CarLoanEvaluationRequestDto request);

    LoanEvaluationResponse evaluateLoan(Long loanId);

    LoanDisbursementResponse disburseLoan(Long loanId);

    List<LoanEmiScheduleResponse> getEmiSchedule(Long loanId);

    void payEmi(Long loanId, Long emiId, LocalDate paymentDate);
}
