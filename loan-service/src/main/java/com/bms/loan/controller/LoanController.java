package com.bms.loan.controller;

import com.bms.loan.dto.request.ApproveLoanRequestDto;
import com.bms.loan.dto.request.DisburseLoanRequestDto;
import com.bms.loan.dto.request.LoanApplicationDto;
import com.bms.loan.dto.request.RepaymentRequestDto;
import com.bms.loan.dto.response.*;
import com.bms.loan.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/applications")
    public LoanApplicationResponse applyLoan(@Valid @RequestBody LoanApplicationDto dto) {
        return loanService.applyLoan(dto);
    }

    @PostMapping("/applications/{id}/evaluate")
    public LoanEvaluationResponse evaluateLoan(@PathVariable Long id, @RequestParam Integer creditScore,
                                        @RequestParam BigDecimal monthlyIncome,
                                        @RequestParam BigDecimal existingEmi) {
        return loanService.evaluateLoan(id, creditScore, monthlyIncome, existingEmi);
    }

    @PostMapping("/applications/{id}/approve")
    public LoanApprovalResponse approveLoan(@PathVariable Long id, @RequestBody ApproveLoanRequestDto dto) {
        return loanService.approveLoan(id, dto);
    }

    @PostMapping("/applications/{id}/reject")
    public LoanRejectionResponseDto rejectLoan(@PathVariable Long id, @RequestParam String reason) {
        return loanService.rejectLoan(id, reason);
    }

    @PostMapping("/applications/{id}/disburse")
    public LoanDisbursementResponseDto disburseLoan(@PathVariable Long id, @RequestBody DisburseLoanRequestDto dto) {
        return loanService.disburseLoan(id, dto);
    }

    @GetMapping("/{id}/schedule")
    public List<?> getSchedule(@PathVariable Long id) {
        return loanService.getRepaymentSchedule(id);
    }

    @PostMapping("/{id}/repay")
    public RepaymentResponseDto repayLoan(@PathVariable Long id, @RequestBody RepaymentRequestDto dto) {
        return loanService.repayLoan(id, dto);
    }

    @PatchMapping("/overdue")
    public void markOverdue() {
        loanService.markOverdue();
    }

    @PatchMapping("/{id}/close")
    public LoanCloseResponseDto closeLoan(@PathVariable Long id) {
        return loanService.closeLoan(id);
    }
}
