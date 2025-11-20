package com.bms.account.controller;

import com.bms.account.dtos.AccountResponseDTO;
import com.bms.account.dtos.accountPin.ChangePinRequest;
import com.bms.account.dtos.accountType.CurrentAccountRequestDTO;
import com.bms.account.dtos.accountType.SavingsAccountRequestDTO;
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
@Tag(name = "Account APIs", description = "operations for bank accounts")
@Validated
public class AccountController {

    private final AccountService accountService;

    //  Create Savings Account
    @Operation(summary = "Create a new Savings Account")
    @PostMapping("/savings")
    public ResponseEntity<AccountResponseDTO> createSavingsAccount(
            @Valid @RequestBody SavingsAccountRequestDTO requestDTO) {

        AccountResponseDTO response = accountService.createSavingsAccount(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get account balance using Account Number and 4-digit PIN")
    @GetMapping("/{accountNumber}/pin/{accountPin}/balance")
    public ResponseEntity<BigDecimal> getBalanceByPin(
            @PathVariable String accountNumber,
            @PathVariable String accountPin) {

        BigDecimal balance = accountService.getBalanceByPin(accountNumber, accountPin);
        return ResponseEntity.ok(balance);
    }

    //  Create Current Account
    @Operation(summary = "Create a new Current Account")
    @PostMapping("/current")
    public ResponseEntity<AccountResponseDTO> createCurrentAccount(
            @Valid @RequestBody CurrentAccountRequestDTO requestDTO) {

        AccountResponseDTO response = accountService.createCurrentAccount(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //  Get Account by ID
    @Operation(summary = "Get account by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    //  Get All Accounts
    @Operation(summary = "Get all accounts")
    @GetMapping
    public ResponseEntity<List<AccountResponseDTO>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    //  Update Account (optional: could be split by type later)
    @Operation(summary = "Update account by ID")
    @PutMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody Object requestDTO) {

        return ResponseEntity.ok(accountService.updateAccount(id, requestDTO));
    }

    //  Delete Account
    @Operation(summary = "Delete account by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAccount(@PathVariable Long id) {
        String message = accountService.deleteAccount(id);
        return ResponseEntity.ok(message);
    }

    //  Get Account by Account Number
    @Operation(summary = "Get account by account number")
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountResponseDTO> getAccountByNumber(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }

    @Operation(summary = "Check if account exists by account number")
    @GetMapping("/exists/{accountNumber}")
    public ResponseEntity<Boolean> accountExists(@PathVariable String accountNumber) {
        boolean exists = accountService.existsByAccountNumber(accountNumber);
        return ResponseEntity.ok(exists);
    }


    //  Get Balance
    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String accountNumber) {
        BigDecimal balance = accountService.getBalance(accountNumber);
        return ResponseEntity.ok(balance);
    }

    //  Update Balance (Deposit / Withdraw)
    @PostMapping("/{accountNumber}/balance")
    public ResponseEntity<?> updateBalance(
            @PathVariable String accountNumber,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("transactionType") String transactionType) {
        accountService.updateBalance(accountNumber, amount, transactionType);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get all accounts by CIF number")
    @GetMapping("/cif/{cifNumber}")
    public ResponseEntity<List<AccountResponseDTO>> getAccountsByCif(@PathVariable String cifNumber) {
        return ResponseEntity.ok(accountService.getAccountsByCif(cifNumber));
    }

    @Operation(summary = "Activate all accounts for a given CIF number")
    @PutMapping("/{cifNumber}/activate")
    public ResponseEntity<String> activateByCif(@PathVariable String cifNumber) {
        String message = accountService.activateAccountsByCif(cifNumber);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/{accountNumber}/change-pin")
    public ResponseEntity<String> changeAccountPin(
            @PathVariable String accountNumber,
            @RequestBody ChangePinRequest request) {
        String result = accountService.changeAccountPin(accountNumber, request);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Verify if the entered account PIN is correct or not")
    @PostMapping("/verify-pin")
    public ResponseEntity<Boolean> verifyAccountPin(
            @RequestParam String accountNumber,
            @RequestParam int accountPin) {
        boolean isValid = accountService.verifyAccountPin(accountNumber, accountPin);
        return ResponseEntity.ok(isValid);
    }
}
