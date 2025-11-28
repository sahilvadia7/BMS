package com.bms.loan.controller;

import com.bms.loan.dto.request.loan.LoanPrepaymentRequest;
import com.bms.loan.dto.request.car.CarLoanEvaluationRequestDto;
import com.bms.loan.dto.request.loan.LoanApplicationRequest;
import com.bms.loan.dto.request.education.EducationVerificationRequestDto;
import com.bms.loan.dto.response.car.CarLoanEvaluationByBankResponse;
import com.bms.loan.dto.response.education.EducationEvaluationResponse;
import com.bms.loan.dto.response.emi.CustomerTimelyPaidEmiResponseDTO;
import com.bms.loan.dto.response.emi.EmiSummary;
import com.bms.loan.dto.response.emi.LoanEmiScheduleResponse;
import com.bms.loan.dto.response.loan.*;
import com.bms.loan.enums.DocumentType;
import com.bms.loan.enums.LoanType;
import com.bms.loan.service.LoanApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@Tag(name = "Loan Application Management", description = "Endpoints for applying, evaluating, and managing loans")
public class LoanApplicationController {

    private final LoanApplicationService loanService;

    public LoanApplicationController(LoanApplicationService loanService) {
        this.loanService = loanService;
    }

    // apply loan
    @Operation(summary = "Apply for a loan", description = "Access: Customer")
    @PostMapping("/apply")
    public ResponseEntity<LoanApplicationResponse> applyLoan(@RequestBody LoanApplicationRequest request) {
        return ResponseEntity.ok(loanService.applyLoan(request));
    }

    // Evaluate Loan by ID
    @Operation(summary = "Evaluate loan application", description = "Access: Internal (Loan Officer)")
    @PostMapping("/{loanId}/evaluate")
    public ResponseEntity<LoanEvaluationResponse> evaluateLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.evaluateLoan(loanId));
    }

    // Update Car Loan Evaluation Data
    @Operation(summary = "Update car loan evaluation", description = "Access: Internal (Loan Officer)")
    @PutMapping("/{loanId}/car-evaluation")
    public ResponseEntity<CarLoanEvaluationByBankResponse> updateCarLoanEvaluation(
            @PathVariable Long loanId,
            @RequestBody CarLoanEvaluationRequestDto request) {
        CarLoanEvaluationByBankResponse updated = loanService.updateEvaluationData(loanId, request);
        return ResponseEntity.ok(updated);
    }

    // Update Car Loan Evaluation Data
    @Operation(summary = "Verify education background", description = "Access: Internal (Loan Officer)")
    @PutMapping("/{loanId}/education-verify")
    public ResponseEntity<EducationEvaluationResponse> verifyEducationBackground(
            @PathVariable Long loanId,
            @RequestBody EducationVerificationRequestDto request) {
        EducationEvaluationResponse updated = loanService.verifyEducationBackground(loanId, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Disburse loan", description = "Access: Admin")
    @PostMapping("/{loanId}/disburse")
    public ResponseEntity<LoanDisbursementResponse> disburseLoan(@PathVariable Long loanId) {
        LoanDisbursementResponse response = loanService.disburseLoan(loanId);
        return ResponseEntity.ok(response);
    }

    // Fetch EMI Schedule
    @Operation(summary = "Get EMI schedule", description = "Access: Customer, Admin")
    @GetMapping("/{loanId}/schedule")
    public ResponseEntity<List<LoanEmiScheduleResponse>> getEmiSchedule(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getEmiSchedule(loanId));
    }

    // Pay EMI
    @Operation(summary = "Pay EMI", description = "Access: Customer")
    @PostMapping("/{loanId}/emi/{emiId}/pay")
    public ResponseEntity<String> payEmi(@PathVariable Long loanId, @PathVariable Long emiId) {
        loanService.payEmi(loanId, emiId, LocalDate.now());
        return ResponseEntity.ok("EMI payment processed successfully");
    }

    // Get loan Details by loan id
    @Operation(summary = "Get loan details by ID", description = "Access: Customer, Admin")
    @GetMapping("/{loanId}/detail")
    public ResponseEntity<LoanDetailsResponse> getLoanDetails(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getLoanDetailsById(loanId));
    }

    // get all loan by CIFNumber
    @Operation(summary = "Get all loans by CIF Number", description = "Access: Admin")
    @GetMapping("/{cifNumber}/all")
    public ResponseEntity<List<LoanDetailsResponse>> getLoansByCif(@PathVariable String cifNumber) {
        return ResponseEntity.ok(loanService.getLoansByCif(cifNumber));
    }

    // get all emi list for loan
    @Operation(summary = "Get all EMIs for a loan", description = "Access: Customer, Admin")
    @GetMapping("/{loanId}/emi/all")
    public ResponseEntity<List<EmiSummary>> getAllEmis(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getAllEmisByLoanId(loanId));
    }

    // get single emi by loan id and emi id
    @Operation(summary = "Get EMI by ID", description = "Access: Customer, Admin")
    @GetMapping("/{loanId}/emi/{emiId}")
    public ResponseEntity<EmiSummary> getEmiById(@PathVariable Long loanId, @PathVariable Long emiId) {
        return ResponseEntity.ok(loanService.getEmiById(loanId, emiId));
    }

    @Operation(summary = "Get required documents for loan type", description = "Access: Public")
    @GetMapping("/required/{loanType}")
    public ResponseEntity<List<DocumentType>> getRequiredDocuments(@PathVariable LoanType loanType) {
        return ResponseEntity.ok(DocumentType.getRequiredForLoan(loanType));
    }

    @Operation(summary = "Make loan prepayment", description = "Access: Customer")
    @PostMapping("/{loanId}/prepayment")
    public ResponseEntity<LoanPrepaymentResponse> makePrepayment(
            @PathVariable Long loanId,
            @RequestBody LoanPrepaymentRequest request) {
        return ResponseEntity.ok(loanService.makePrepayment(loanId, request));
    }

    @Operation(summary = "Get customer timely paid EMI details", description = "Access: Admin")
    @GetMapping("/{cifNumber}/customer")
    public ResponseEntity<CustomerTimelyPaidEmiResponseDTO> customerTimelyPaidEmiDetails(
            @PathVariable String cifNumber) {
        return ResponseEntity.ok(loanService.customerTimelyPaidEmiDetails(cifNumber));
    }

}
