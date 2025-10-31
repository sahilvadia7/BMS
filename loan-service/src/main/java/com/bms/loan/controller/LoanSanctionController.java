package com.bms.loan.controller;

import com.bms.loan.dto.request.home.LoanSanctionRequest;
import com.bms.loan.dto.response.home.LoanSanctionResponseDTO;
import com.bms.loan.service.HomeLoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanSanctionController {

    private final HomeLoanService homeLoanService;

    public LoanSanctionController(HomeLoanService homeLoanService) {
        this.homeLoanService = homeLoanService;
    }

    @PostMapping("/{loanId}/sanction")
    public ResponseEntity<LoanSanctionResponseDTO> sanctionHomeLoan(
            @PathVariable Long loanId,
            @RequestBody LoanSanctionRequest request) {

        LoanSanctionResponseDTO response = homeLoanService.sanctionHomeLoan(loanId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{loanId}/eSign")
    public ResponseEntity<?> eSignSanctionLatter(@PathVariable Long loanId) {
        homeLoanService.eSignSanctionLatter(loanId);
        return ResponseEntity.ok("eSign Successfully");
    }

}
