package com.bms.loan.controller;

import com.bms.loan.dto.request.car.CarLoanEvaluationRequestDto;
import com.bms.loan.dto.request.LoanApplicationRequest;
import com.bms.loan.dto.response.car.CarLoanEvaluationByBankResponse;
import com.bms.loan.dto.response.emi.EmiSummary;
import com.bms.loan.dto.response.emi.LoanEmiScheduleResponse;
import com.bms.loan.dto.response.loan.LoanApplicationResponse;
import com.bms.loan.dto.response.loan.LoanDetailsResponse;
import com.bms.loan.dto.response.loan.LoanDisbursementResponse;
import com.bms.loan.dto.response.loan.LoanEvaluationResponse;
import com.bms.loan.enums.DocumentType;
import com.bms.loan.enums.LoanType;
import com.bms.loan.service.LoanApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/v1/loans")
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

    // Fetch EMI Schedule
    @GetMapping("/{loanId}/schedule")
    public ResponseEntity<List<LoanEmiScheduleResponse>> getEmiSchedule(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getEmiSchedule(loanId));
    }


    // Pay EMI
    @PostMapping("/{loanId}/emi/{emiId}/pay")
    public ResponseEntity<String> payEmi(@PathVariable Long loanId, @PathVariable Long emiId){
            loanService.payEmi(loanId, emiId , LocalDate.now());
        return ResponseEntity.ok("EMI payment processed successfully");
    }

    // Get loan Details by loan id
    @GetMapping("/{loanId}/detail")
    public ResponseEntity<LoanDetailsResponse> getLoanDetails(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getLoanDetailsById(loanId));
    }


    // get all loan by CIFNumber
    @GetMapping("/{cifNumber}/all")
    public ResponseEntity<List<LoanDetailsResponse>> getLoansByCif(@PathVariable String cifNumber) {
        return ResponseEntity.ok(loanService.getLoansByCif(cifNumber));
    }

    // get all emi list for loan
    @GetMapping("/{loanId}/emi/all")
    public ResponseEntity<List<EmiSummary>> getAllEmis(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getAllEmisByLoanId(loanId));
    }

    // get single emi by loan id and emi id
    @GetMapping("/{loanId}/emi/{emiId}")
    public ResponseEntity<EmiSummary> getEmiById(@PathVariable Long loanId, @PathVariable Long emiId) {
        return ResponseEntity.ok(loanService.getEmiById(loanId, emiId));
    }

    @GetMapping("/required/{loanType}")
    public ResponseEntity<List<DocumentType>> getRequiredDocuments(@PathVariable LoanType loanType) {
        return ResponseEntity.ok(DocumentType.getRequiredForLoan(loanType));
    }
}
