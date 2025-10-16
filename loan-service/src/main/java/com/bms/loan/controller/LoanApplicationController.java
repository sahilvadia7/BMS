package com.bms.loan.controller;

import com.bms.loan.dto.request.LoanApplicationRequest;
import com.bms.loan.dto.response.LoanApplicationResponse;
import com.bms.loan.service.LoanApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/loans")
public class LoanApplicationController {

    private final LoanApplicationService loanService;

    public LoanApplicationController(LoanApplicationService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/apply")
    public ResponseEntity<LoanApplicationResponse> applyLoan(@RequestBody LoanApplicationRequest request) {
        return ResponseEntity.ok(loanService.applyLoan(request));
    }
}
