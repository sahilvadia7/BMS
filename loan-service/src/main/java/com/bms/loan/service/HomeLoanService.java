package com.bms.loan.service;

import com.bms.loan.dto.request.home.HomeVerificationRequestDto;
import com.bms.loan.dto.request.home.LoanSanctionRequest;
import com.bms.loan.dto.response.home.HomeLoanDisbursementResponseDTO;
import com.bms.loan.dto.response.home.HomeLoanSanctionResponseDTO;
import com.bms.loan.dto.response.home.HomeVerificationResponse;
import com.bms.loan.dto.response.loan.LoanEvaluationResponse;

public interface HomeLoanService {

    // Capture verification details from officer/site visit
    HomeVerificationResponse verifyProperty(HomeVerificationRequestDto verificationRequest);

    // Evaluate home loan eligibility based on property, income, etc.
    LoanEvaluationResponse evaluateLoan(Long loanId);

    // Sanction/approve home loan based on evaluation
    HomeLoanSanctionResponseDTO sanctionLoan(Long loanId, String sanctionedBy);

    // Disburse loan amount to customer or builder
    HomeLoanDisbursementResponseDTO disburseLoan(Long loanId);

    HomeLoanSanctionResponseDTO sanctionHomeLoan(Long loanId, LoanSanctionRequest request);

    void eSignSanctionLatter(Long loanId);
}
