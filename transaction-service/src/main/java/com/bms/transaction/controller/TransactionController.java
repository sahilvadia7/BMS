package com.bms.transaction.controller;

import com.bms.transaction.dto.request.SearchTransactionsRequest;
import com.bms.transaction.dto.request.TransactionRequest;
import com.bms.transaction.dto.request.TransferRequest;
import com.bms.transaction.dto.response.TransactionResponseDto;
import com.bms.transaction.dto.response.TransactionSummaryDto;
import com.bms.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Create a transaction", responses = {
            @ApiResponse(responseCode = "200", description = "Transaction created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Account or branch not found")
    })
    @PostMapping
    public ResponseEntity<TransactionResponseDto> createTransaction(
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.createTransaction(request));
    }

    @Operation(summary = "Create a transfer", responses = {
            @ApiResponse(responseCode = "200", description = "Transfer created"),
            @ApiResponse(responseCode = "400", description = "Invalid input or insufficient funds"),
            @ApiResponse(responseCode = "404", description = "Account or branch not found")
    })
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDto> createTransfer(
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transactionService.transfer(request));
    }

    @Operation(summary = "Get transaction by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })


    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDto> getTransactionById(
            @PathVariable Long transactionId) {
        return ResponseEntity.ok(transactionService.getTransactionById(transactionId));
    }

    @Operation(summary = "Search transactions", responses = {
            @ApiResponse(responseCode = "200", description = "List of transactions"),
            @ApiResponse(responseCode = "400", description = "Invalid search criteria")
    })
    @GetMapping("/search")
    public ResponseEntity<List<TransactionResponseDto>> searchTransactions(
            @Valid @ModelAttribute SearchTransactionsRequest request) {
        return ResponseEntity.ok(transactionService.searchTransactions(request));
    }

    @Operation(summary = "Get transaction summary", responses = {
            @ApiResponse(responseCode = "200", description = "Transaction summary"),
            @ApiResponse(responseCode = "404", description = "Account or branch not found")
    })
    @GetMapping("/summary")
    public ResponseEntity<TransactionSummaryDto> getTransactionSummary(
            @RequestParam Long accountId, @RequestParam Long branchId) {
        return ResponseEntity.ok(transactionService.getTransactionSummary(accountId, branchId));
    }
}