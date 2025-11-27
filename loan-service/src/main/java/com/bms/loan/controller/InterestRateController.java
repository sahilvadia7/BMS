package com.bms.loan.controller;

import com.bms.loan.dto.request.InterestRateRequest;
import com.bms.loan.dto.response.InterestRateResponse;
import com.bms.loan.service.InterestRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interest-rates")
@Tag(name = "Interest Rate Management", description = "Endpoints for managing loan interest rates")
public class InterestRateController {

    private final InterestRateService service;

    public InterestRateController(InterestRateService service) {
        this.service = service;
    }

    // Get all
    @Operation(summary = "Get all interest rates", description = "Access: Public")
    @GetMapping
    public ResponseEntity<List<InterestRateResponse>> getAllRates() {
        return ResponseEntity.ok(service.getAllRates());
    }

    // Get by ID
    @Operation(summary = "Get interest rate by ID", description = "Access: Public")
    @GetMapping("/{id}")
    public ResponseEntity<InterestRateResponse> getRateById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getRateById(id));
    }

    // Get by Loan Type
    @Operation(summary = "Get interest rate by loan type", description = "Access: Public")
    @GetMapping("/type/{loanType}")
    public ResponseEntity<InterestRateResponse> getRateByLoanType(@PathVariable String loanType) {
        return ResponseEntity.ok(service.getRateByLoanType(loanType));
    }

    // Update existing
    @Operation(summary = "Update interest rate", description = "Access: Admin")
    @PutMapping("/{id}")
    public ResponseEntity<InterestRateResponse> updateRate(@PathVariable Long id,
            @RequestBody InterestRateRequest request) {
        return ResponseEntity.ok(service.updateRate(id, request));
    }

    // Delete
    @Operation(summary = "Delete interest rate", description = "Access: Admin")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRate(@PathVariable Long id) {
        service.deleteRate(id);
        return ResponseEntity.ok("Interest rate with id " + id + " deleted successfully.");
    }

}
