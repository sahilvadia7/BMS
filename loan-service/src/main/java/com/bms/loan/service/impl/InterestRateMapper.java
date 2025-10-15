package com.bms.loan.service.impl;

import com.bms.loan.dto.request.InterestRateRequest;
import com.bms.loan.dto.response.InterestRateResponse;
import com.bms.loan.entity.InterestRate;
import org.springframework.stereotype.Component;

@Component
public class InterestRateMapper {

    public InterestRate toEntity(InterestRateRequest request) {
        return InterestRate.builder()
                .loanType(request.getLoanType().toUpperCase())
                .baseRate(request.getBaseRate())
                .maxLTV(request.getMaxLTV())
                .minTenure(request.getMinTenure())
                .maxTenure(request.getMaxTenure())
                .build();
    }

    public InterestRateResponse toResponse(InterestRate entity) {
        return InterestRateResponse.builder()
                .id(entity.getId())
                .loanType(entity.getLoanType())
                .baseRate(entity.getBaseRate())
                .maxLTV(entity.getMaxLTV())
                .minTenure(entity.getMinTenure())
                .maxTenure(entity.getMaxTenure())
                .build();
    }
}
