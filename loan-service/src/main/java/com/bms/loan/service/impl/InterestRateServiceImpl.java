package com.bms.loan.service.impl;

import com.bms.loan.Repository.InterestRateRepository;
import com.bms.loan.dto.request.InterestRateRequest;
import com.bms.loan.dto.response.InterestRateResponse;
import com.bms.loan.entity.InterestRate;
import com.bms.loan.service.InterestRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterestRateServiceImpl implements InterestRateService {

    @Autowired
    private InterestRateRepository repository;

    @Autowired
    private InterestRateMapper mapper;


    public List<InterestRateResponse> getAllRates() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }


    public InterestRateResponse getRateById(Long id) {
        InterestRate rate = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interest rate not found with id: " + id));
        return mapper.toResponse(rate);
    }

    public InterestRateResponse getRateByLoanType(String loanType) {
        InterestRate rate = repository.findByLoanType(loanType.toUpperCase());
        if (rate == null)
            throw new RuntimeException("Interest rate not found for loan type: " + loanType);
        return mapper.toResponse(rate);
    }


    public InterestRateResponse createRate(InterestRateRequest request) {
        InterestRate entity = mapper.toEntity(request);
        InterestRate saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    public InterestRateResponse updateRate(Long id, InterestRateRequest request) {
        InterestRate existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interest rate not found with id: " + id));

        existing.setLoanType(request.getLoanType().toUpperCase());
        existing.setBaseRate(request.getBaseRate());
        existing.setMaxLTV(request.getMaxLTV());
        existing.setMinTenure(request.getMinTenure());
        existing.setMaxTenure(request.getMaxTenure());

        return mapper.toResponse(repository.save(existing));
    }

    public void deleteRate(Long id) {
        repository.deleteById(id);
    }
}
