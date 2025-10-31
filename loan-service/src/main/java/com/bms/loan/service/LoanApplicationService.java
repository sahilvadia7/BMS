package com.bms.loan.service;

import com.bms.loan.dto.request.car.CarLoanEvaluationRequestDto;
import com.bms.loan.dto.request.LoanApplicationRequest;
import com.bms.loan.dto.request.education.EducationVerificationRequestDto;
import com.bms.loan.dto.response.car.CarLoanEvaluationByBankResponse;
import com.bms.loan.dto.response.education.EducationEvaluationResponse;
import com.bms.loan.dto.response.emi.EmiSummary;
import com.bms.loan.dto.response.emi.LoanEmiScheduleResponse;
import com.bms.loan.dto.response.home.LoanSanctionResponseDTO;
import com.bms.loan.dto.response.loan.LoanApplicationResponse;
import com.bms.loan.dto.response.loan.LoanDetailsResponse;
import com.bms.loan.dto.response.loan.LoanDisbursementResponse;
import com.bms.loan.dto.response.loan.LoanEvaluationResponse;

import java.time.LocalDate;
import java.util.List;

public interface LoanApplicationService {
    LoanApplicationResponse applyLoan(LoanApplicationRequest request);

    CarLoanEvaluationByBankResponse updateEvaluationData(Long loanId, CarLoanEvaluationRequestDto request);

    EducationEvaluationResponse verifyEducationBackground(Long loanId, EducationVerificationRequestDto request);

    LoanEvaluationResponse evaluateLoan(Long loanId);

    LoanDisbursementResponse disburseLoan(Long loanId);

    List<LoanEmiScheduleResponse> getEmiSchedule(Long loanId);

    void payEmi(Long loanId, Long emiId, LocalDate paymentDate);

    LoanDetailsResponse getLoanDetailsById(Long loanId);

    List<LoanDetailsResponse> getLoansByCif(String cifNumber);

    List<EmiSummary> getAllEmisByLoanId(Long loanId);

    EmiSummary getEmiById(Long loanId, Long emiId);

    // Sanction/approve loan based on evaluation
//    LoanSanctionResponseDTO sanctionLoan(Long loanId);
}
