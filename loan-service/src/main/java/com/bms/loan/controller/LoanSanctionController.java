package com.bms.loan.controller;

import com.bms.loan.dto.request.home.LoanSanctionRequest;
import com.bms.loan.dto.response.home.LoanSanctionResponseDTO;
import com.bms.loan.service.LoanEvolutionAndSanctionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loans")
@Tag(name = "Loan Sanction Management", description = "Endpoints for sanctioning loans")
public class LoanSanctionController {

    private final LoanEvolutionAndSanctionService homeLoanService;

    public LoanSanctionController(LoanEvolutionAndSanctionService homeLoanService) {
        this.homeLoanService = homeLoanService;
    }

    @Operation(summary = "Sanction home loan", description = "Access: Internal (Loan Officer)")
    @PostMapping("/{loanId}/sanction")
    public ResponseEntity<LoanSanctionResponseDTO> sanctionHomeLoan(
            @PathVariable Long loanId,
            @RequestBody LoanSanctionRequest request) {

        LoanSanctionResponseDTO response = homeLoanService.sanctionHomeLoan(loanId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "eSign sanction letter", description = "Access: Customer")
    @PatchMapping("/{loanId}/eSign")
    public ResponseEntity<?> eSignSanctionLatter(@PathVariable Long loanId) {
        homeLoanService.eSignSanctionLatter(loanId);
        return ResponseEntity.ok("eSign Successfully");
    }

}
