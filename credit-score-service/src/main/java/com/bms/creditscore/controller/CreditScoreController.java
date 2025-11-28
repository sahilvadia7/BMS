package com.bms.creditscore.controller;

import com.bms.creditscore.dto.request.CreditCheckRequest;
import com.bms.creditscore.dto.request.CreditScoreRequest;
import com.bms.creditscore.dto.response.CreditCheckResponse;
import com.bms.creditscore.dto.response.CreditScoreResponse;
import com.bms.creditscore.service.CreditScoreService;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller for Credit Score Service.
 * Handles score calculation, eligibility checks, and history retrieval.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/credit-score")
@Tag(name = "Credit Score Management", description = "Endpoints for calculating and retrieving credit scores")
public class CreditScoreController {

    private final CreditScoreService service;

    public CreditScoreController(CreditScoreService service) {
        this.service = service;
    }

    /**
     * Calculate or recalculate a credit score.
     * Used by admin panels, internal batch jobs, or via direct API testing.
     */
    @Operation(summary = "Calculate credit score", description = "Access: Admin, Internal")
    @PostMapping("/calculate")
    public ResponseEntity<CreditScoreResponse> calculateScore(@RequestBody CreditScoreRequest request) {
        log.info("Request received to calculate credit score for customerId: {}", request.getCustomerId());

        try {
            CreditScoreResponse response = service.calculate(request.getCustomerId(), request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating credit score: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Fetch latest score for a given customer.
     */
    @Operation(summary = "Get latest credit score", description = "Access: Customer, Admin")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CreditScoreResponse> getLatestScore(@PathVariable Long customerId) {
        log.info("Fetching latest score for customerId: {}", customerId);

        try {
            CreditScoreResponse response = service.getLatestByCustomer(customerId);

            if (response == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching latest score: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Check if a customer is eligible for a credit/loan product.
     */
    @Operation(summary = "Check credit eligibility", description = "Access: Internal (Loan Service)")
    @PostMapping("/check")
    public ResponseEntity<CreditCheckResponse> checkEligibility(@RequestBody CreditCheckRequest request) {
        log.info("Checking credit eligibility for customerId: {}", request.getCustomerId());

        try {
            CreditCheckResponse response = service.checkEligibility(request);
            HttpStatus status = response.getMessage().equalsIgnoreCase("Customer ID is required")
                    ? HttpStatus.BAD_REQUEST
                    : HttpStatus.OK;
            return ResponseEntity.status(status).body(response);
        } catch (Exception e) {
            log.error("Error checking credit eligibility: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Fetch full credit history for a customer.
     */
    @Operation(summary = "Get credit history", description = "Access: Customer, Admin")
    @GetMapping("/customer/{customerId}/history")
    public ResponseEntity<List<CreditScoreResponse>> getCreditHistory(@PathVariable Long customerId) {
        log.info("Fetching credit history for customerId: {}", customerId);

        try {
            List<CreditScoreResponse> history = service.getCreditHistory(customerId);

            if (history == null || history.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error fetching credit history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Fetch all customer credit profiles (Admin use only).
     */
    @Operation(summary = "Get all credit profiles", description = "Access: Admin")
    @GetMapping("/all")
    public ResponseEntity<List<CreditScoreResponse>> getAllProfiles() {
        log.info("Fetching all credit profiles");

        try {
            List<CreditScoreResponse> profiles = service.getAllProfiles();
            return ResponseEntity.ok(profiles);
        } catch (Exception e) {
            log.error("Error fetching all credit profiles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get current repo rate in use.
     */
    @Operation(summary = "Get repo rate", description = "Access: Public/Internal")
    @GetMapping("/repo-rate")
    public ResponseEntity<Double> getRepoRate() {
        log.info("Fetching repo rate");
        try {
            return ResponseEntity.ok(service.getRepoRate());
        } catch (Exception e) {
            log.error("Error fetching repo rate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check for this service.
     */
    @Operation(summary = "Health check", description = "Access: Public")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Credit Score Service is running");
    }
}
