package com.bms.loan.controller;

import com.bms.loan.dto.request.home.LoanSanctionRequest;
import com.bms.loan.dto.response.home.HomeLoanSanctionResponseDTO;
import com.bms.loan.service.HomeLoanService;
import com.bms.loan.service.impl.SanctionLetterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanSanctionController {

    private final HomeLoanService homeLoanService;

    public LoanSanctionController(HomeLoanService homeLoanService) {
        this.homeLoanService = homeLoanService;
    }

    @PostMapping("/{loanId}/sanction")
    public ResponseEntity<HomeLoanSanctionResponseDTO> sanctionHomeLoan(
            @PathVariable Long loanId,
            @RequestBody LoanSanctionRequest request) {

        HomeLoanSanctionResponseDTO response = homeLoanService.sanctionHomeLoan(loanId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{loanId}/eSign")
    public ResponseEntity<?> eSignSanctionLatter(@PathVariable Long loanId) {
        homeLoanService.eSignSanctionLatter(loanId);
        return ResponseEntity.ok("eSign Successfully");
    }

}
