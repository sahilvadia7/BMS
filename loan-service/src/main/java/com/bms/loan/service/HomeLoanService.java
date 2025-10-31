package com.bms.loan.service;

import com.bms.loan.dto.request.home.HomeVerificationRequestDto;
import com.bms.loan.dto.request.home.LoanSanctionRequest;
import com.bms.loan.dto.response.home.HomeLoanDisbursementResponseDTO;
import com.bms.loan.dto.response.home.LoanSanctionResponseDTO;
import com.bms.loan.dto.response.home.HomeVerificationResponse;
import com.bms.loan.dto.response.loan.LoanEvaluationResponse;

public interface HomeLoanService {

    // Capture verification details from officer/site visit
    HomeVerificationResponse verifyProperty(HomeVerificationRequestDto verificationRequest);

    // Evaluate home loan eligibility based on property, income, etc.
    LoanEvaluationResponse evaluateLoan(Long loanId);

    LoanSanctionResponseDTO sanctionHomeLoan(Long loanId, LoanSanctionRequest request);

    void eSignSanctionLatter(Long loanId);
}
