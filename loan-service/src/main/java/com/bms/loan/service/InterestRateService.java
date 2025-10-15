package com.bms.loan.service;

import com.bms.loan.dto.request.InterestRateRequest;
import com.bms.loan.dto.response.InterestRateResponse;

import java.util.List;

public interface InterestRateService {

    List<InterestRateResponse> getAllRates();

    InterestRateResponse getRateById(Long id);

    InterestRateResponse getRateByLoanType(String loanType);

    InterestRateResponse createRate(InterestRateRequest request);

    InterestRateResponse updateRate(Long id, InterestRateRequest request);

    void deleteRate(Long id);

}
