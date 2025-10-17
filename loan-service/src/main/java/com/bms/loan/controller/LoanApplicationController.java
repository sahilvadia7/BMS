package com.bms.loan.controller;

import com.bms.loan.dto.request.CarLoanEvaluationRequestDto;
import com.bms.loan.dto.request.LoanApplicationRequest;
import com.bms.loan.dto.response.CarLoanEvaluationByBankResponse;
import com.bms.loan.dto.response.LoanApplicationResponse;
import com.bms.loan.dto.response.LoanDisbursementResponse;
import com.bms.loan.dto.response.LoanEvaluationResponse;
import com.bms.loan.service.LoanApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/loans")
public class LoanApplicationController {

    private final LoanApplicationService loanService;

    public LoanApplicationController(LoanApplicationService loanService) {
        this.loanService = loanService;
    }

    // apply loan
    @PostMapping("/apply")
    public ResponseEntity<LoanApplicationResponse> applyLoan(@RequestBody LoanApplicationRequest request) {
        return ResponseEntity.ok(loanService.applyLoan(request));
    }

    //Update Car Loan Evaluation Data
    @PutMapping("/{loanId}/evaluation")
    public ResponseEntity<CarLoanEvaluationByBankResponse> updateCarLoanEvaluation(
            @PathVariable Long loanId,
            @RequestBody CarLoanEvaluationRequestDto request) {
        CarLoanEvaluationByBankResponse updated = loanService.updateEvaluationData(loanId, request);
        return ResponseEntity.ok(updated);
    }

    // Evaluate Loan by ID
    @PostMapping("/{loanId}/evaluate")
    public ResponseEntity<LoanEvaluationResponse> evaluateLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.evaluateLoan(loanId));
    }

    @PostMapping("/{loanId}/disburse")
    public ResponseEntity<LoanDisbursementResponse> disburseLoan(@PathVariable Long loanId) {
        LoanDisbursementResponse response = loanService.disburseLoan(loanId);
        return ResponseEntity.ok(response);
    }
}
