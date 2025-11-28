package com.bms.loan.controller;

import com.bms.loan.dto.request.home.HomeVerificationRequestDto;
import com.bms.loan.dto.response.home.HomeVerificationResponse;
import com.bms.loan.service.HomeLoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loans/home")
@Tag(name = "Home Loan Management", description = "Endpoints for home loan verification and processing")
public class HomeLoanController {

    private final HomeLoanService homeLoanService;

    public HomeLoanController(HomeLoanService homeLoanService) {
        this.homeLoanService = homeLoanService;
    }

    // verify property
    @Operation(summary = "Verify home property and documents", description = "Access: Internal (Loan Officer)")
    @PostMapping("/verify")
    public ResponseEntity<HomeVerificationResponse> verifyProperty(@RequestBody HomeVerificationRequestDto request) {
        HomeVerificationResponse response = homeLoanService.verifyProperty(request);
        return ResponseEntity.ok(response);
    }

}
