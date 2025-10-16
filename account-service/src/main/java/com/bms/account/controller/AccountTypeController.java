package com.bms.account.controller;

import com.bms.account.dtos.accountType.AccountTypeRequestDto;
import com.bms.account.dtos.accountType.AccountTypeResponseDto;
import com.bms.account.dtos.accountType.AccountTypeUpdateDto;
import com.bms.account.services.AccountTypeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account-types")
@RequiredArgsConstructor
public class AccountTypeController {
    private final AccountTypeService accountTypeService;

    @Operation(summary = "Create new account type")
    @PostMapping
    public ResponseEntity<AccountTypeResponseDto> createAccountType(@RequestBody AccountTypeRequestDto dto) {
        return new ResponseEntity<>(accountTypeService.createAccountType(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Update existing account type")
    @PutMapping("/{id}")
    public ResponseEntity<AccountTypeResponseDto> updateAccountType(@PathVariable Long id, @RequestBody AccountTypeUpdateDto dto) {
        return ResponseEntity.ok(accountTypeService.updateAccountType(id, dto));
    }

    @Operation(summary = "Delete account type by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccountType(@PathVariable Long id) {
        accountTypeService.deleteAccountType(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get account type by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AccountTypeResponseDto> getAccountTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(accountTypeService.getAccountTypeById(id));
    }

    @Operation(summary = "Get all account types")
    @GetMapping
    public ResponseEntity<List<AccountTypeResponseDto>> getAllAccountTypes() {
        return ResponseEntity.ok(accountTypeService.getAllAccountTypes());
    }
}
