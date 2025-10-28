package com.bms.loan.controller;

import com.bms.loan.dto.request.home.HomeVerificationRequestDto;
import com.bms.loan.dto.response.home.HomeLoanDisbursementResponseDTO;
import com.bms.loan.dto.response.home.HomeLoanSanctionResponseDTO;
import com.bms.loan.dto.response.home.HomeVerificationResponse;
import com.bms.loan.dto.response.loan.LoanEvaluationResponse;
import com.bms.loan.service.HomeLoanService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans/home")
@RequiredArgsConstructor
public class HomeLoanController {

    private final HomeLoanService homeLoanService;

    // verify property
    @Operation(summary = "Verify home property and documents")
    @PostMapping("/verify")
    public ResponseEntity<HomeVerificationResponse> verifyProperty(@RequestBody HomeVerificationRequestDto request) {
        HomeVerificationResponse response = homeLoanService.verifyProperty(request);
        return ResponseEntity.ok(response);
    }

    // evaluate loan
    @Operation(summary = "Evaluate home loan eligibility and approval")
    @PostMapping("/{loanId}/evaluate")
    public ResponseEntity<LoanEvaluationResponse> evaluateHomeLoan(@PathVariable Long loanId) {
        LoanEvaluationResponse response = homeLoanService.evaluateLoan(loanId);
        return ResponseEntity.ok(response);
    }

    // sanction loan
    @Operation(summary = "Sanction home loan after evaluation")
    @PostMapping("/{loanId}/sanction")
    public ResponseEntity<HomeLoanSanctionResponseDTO> sanctionHomeLoan(
            @PathVariable Long loanId,
            @RequestParam String sanctionedBy
    ) {
        HomeLoanSanctionResponseDTO response = homeLoanService.sanctionLoan(loanId, sanctionedBy);
        return ResponseEntity.ok(response);
    }

//    // disburse loan
//    @Operation(summary = "Disburse the sanctioned home loan")
//    @PostMapping("/{loanId}/disburse")
//    public ResponseEntity<HomeLoanDisbursementResponseDTO> disburseHomeLoan(@PathVariable Long loanId) {
//        HomeLoanDisbursementResponseDTO response = homeLoanService.disburseLoan(loanId);
//        return ResponseEntity.ok(response);
//    }

}
