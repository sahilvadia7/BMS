package com.bms.account.controller;

import com.bms.account.dtos.AccountRequestDTO;
import com.bms.account.dtos.AccountResponseDTO;
import com.bms.account.services.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Account APIs", description = "CRUD operations for bank accounts")
@Validated
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Create a new bank account")
    @PostMapping("/{customerId}")
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @PathVariable Long customerId, @RequestBody AccountRequestDTO requestDTO) {
        return new ResponseEntity<>(accountService.createAccount(customerId,requestDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Get account by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @Operation(summary = "Get all accounts")
    @GetMapping
    public ResponseEntity<List<AccountResponseDTO>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @Operation(summary = "Update account by ID")
    @PutMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountRequestDTO requestDTO) {

        return ResponseEntity.ok(accountService.updateAccount(id, requestDTO));
    }

    @Operation(summary = "Delete account by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get account by account number")
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountResponseDTO> getAccountByNumber(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long accountId) {
        BigDecimal balance = accountService.getBalance(accountId);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/{accountId}/balance")
    public ResponseEntity<Void> updateBalance(
            @PathVariable Long accountId,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("transactionType") String transactionType) {

        accountService.updateBalance(accountId, amount, transactionType);
        return ResponseEntity.ok().build();
    }

}
