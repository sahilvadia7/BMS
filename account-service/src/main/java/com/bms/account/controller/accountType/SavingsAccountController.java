//package com.bms.account.controller.accountType;
//
//
//import com.bms.account.dtos.accountType.SavingsAccountRequestDTO;
//import com.bms.account.entities.accountType.SavingsAccount;
//import com.bms.account.services.SavingsAccountService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/accounts/savings")
//@RequiredArgsConstructor
//@Tag(name = "Savings Account", description = "APIs for managing savings accounts")
//public class SavingsAccountController {
//
//    private final SavingsAccountService savingsAccountService;
//
//    @Operation(summary = "Open a new savings account")
//    @PostMapping
//    public ResponseEntity<SavingsAccount> openSavingsAccount(@Valid @RequestBody SavingsAccountRequestDTO dto) {
//        SavingsAccount created = savingsAccountService.openSavingsAccount(dto);
//        return ResponseEntity.ok(created);
//    }
//}
