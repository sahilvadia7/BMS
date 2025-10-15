package com.bms.loan.controller;


import com.bms.loan.dto.request.InterestRateRequest;
import com.bms.loan.dto.response.InterestRateResponse;
import com.bms.loan.service.InterestRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interest-rates")
public class InterestRateController {

    @Autowired
    private InterestRateService service;

    // Get all
    @GetMapping
    public ResponseEntity<List<InterestRateResponse>> getAllRates() {
        return ResponseEntity.ok(service.getAllRates());
    }

    // Get by ID
    @GetMapping("/{id}")
    public ResponseEntity<InterestRateResponse> getRateById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getRateById(id));
    }

    // Get by Loan Type
    @GetMapping("/type/{loanType}")
    public ResponseEntity<InterestRateResponse> getRateByLoanType(@PathVariable String loanType) {
        return ResponseEntity.ok(service.getRateByLoanType(loanType));
    }

    // Create new
    @PostMapping
    public ResponseEntity<InterestRateResponse> createRate(@RequestBody InterestRateRequest request) {
        return ResponseEntity.ok(service.createRate(request));
    }

    // Update existing
    @PutMapping("/{id}")
    public ResponseEntity<InterestRateResponse> updateRate(@PathVariable Long id, @RequestBody InterestRateRequest request) {
        return ResponseEntity.ok(service.updateRate(id, request));
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRate(@PathVariable Long id) {
        service.deleteRate(id);
        return ResponseEntity.ok("Interest rate with id " + id + " deleted successfully.");
    }

}
