package com.bms.loan.service;

import com.bms.loan.dto.request.LoanApplicationRequest;
import com.bms.loan.dto.response.LoanApplicationResponse;

public interface LoanApplicationService {
    LoanApplicationResponse applyLoan(LoanApplicationRequest request);

}
