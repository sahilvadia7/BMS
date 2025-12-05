package com.bms.loan.service;

import com.bms.loan.dto.request.VerifyLoanRequestDto;
import com.bms.loan.dto.request.loan.LoanPrepaymentRequest;
import com.bms.loan.dto.request.loan.LoanApplicationRequest;
import com.bms.loan.dto.request.education.EducationVerificationRequestDto;
import com.bms.loan.dto.response.VerificationResponseDto;
import com.bms.loan.dto.response.emi.CustomerTimelyPaidEmiResponseDTO;
import com.bms.loan.dto.response.emi.EmiSummary;
import com.bms.loan.dto.response.emi.LoanEmiScheduleResponse;
import com.bms.loan.dto.response.loan.*;

import java.time.LocalDate;
import java.util.List;

public interface LoanApplicationService {
    LoanApplicationResponse applyLoan(LoanApplicationRequest request);

    LoanEvaluationResponse evaluateLoan(Long loanId);

    LoanDisbursementResponse disburseLoan(Long loanId);

    List<LoanEmiScheduleResponse> getEmiSchedule(Long loanId);

    void payEmi(Long loanId, Long emiId, LocalDate paymentDate);

    LoanDetailsResponse getLoanDetailsById(Long loanId);

    List<LoanDetailsResponse> getLoansByCif(String cifNumber);

    List<EmiSummary> getAllEmisByLoanId(Long loanId);

    EmiSummary getEmiById(Long loanId, Long emiId);

    LoanPrepaymentResponse makePrepayment(Long loanId, LoanPrepaymentRequest request);

    CustomerTimelyPaidEmiResponseDTO customerTimelyPaidEmiDetails(String cifNumber);

    List<LoanDetailsAdminDto>  getAllLoans();

    VerificationResponseDto verifyLoan(Long loanId, VerifyLoanRequestDto verifyLoanRequestDto);
}
