package com.bms.loan.controller;

import com.bms.loan.service.impl.SanctionLetterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/loans")
public class LoanSanctionController {


    private final SanctionLetterService sanctionLetterService;

    public LoanSanctionController(SanctionLetterService sanctionLetterService) {
        this.sanctionLetterService = sanctionLetterService;
    }

    @PostMapping("/{loanId}/send-sanction-letter")
    public ResponseEntity<String> sendSanctionLetter(@PathVariable Long loanId) throws IOException {
        sanctionLetterService.generateAndSend(loanId);
        return ResponseEntity.ok("Sanction letter sent successfully");
    }

}
