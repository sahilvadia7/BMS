package com.bms.creditscore.service;

import com.bms.creditscore.dto.request.CreditCheckRequest;
import com.bms.creditscore.dto.request.CreditScoreRequest;
import com.bms.creditscore.dto.response.CreditCheckResponse;
import com.bms.creditscore.dto.response.CreditScoreResponse;

import java.util.List;

public interface CreditScoreService {

    double getRepoRate();
    CreditScoreResponse calculate(Long customerId, CreditScoreRequest request);
    CreditScoreResponse getLatestByCustomer(Long customerId);
    CreditCheckResponse checkEligibility(CreditCheckRequest request) ;
    List<CreditScoreResponse> getAllProfiles();
    List<CreditScoreResponse> getCreditHistory(Long customerId);
    }