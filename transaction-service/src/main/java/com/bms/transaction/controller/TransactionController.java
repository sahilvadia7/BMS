package com.bms.transaction.controller;

import com.bms.transaction.dto.request.SearchTransactionsRequest;
import com.bms.transaction.dto.request.TransactionRequest;
import com.bms.transaction.dto.response.TransactionResponseDto;
import com.bms.transaction.dto.response.TransactionSummaryDto;
import com.bms.transaction.enums.TransactionStatus;
import com.bms.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * General purpose endpoint for all types of transactions
     * Handles Deposit, Withdrawal, Transfer, Loan Disbursement, EMI Deduction, and Refund
     */
    @Operation(
            summary = "Create a transaction (Deposit, Withdrawal, Transfer, Loan, EMI, Refund)",
            description = "Handles all transaction types in one unified API endpoint.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transaction processed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input or insufficient funds"),
                    @ApiResponse(responseCode = "404", description = "Account or destination not found"),
                    @ApiResponse(responseCode = "500", description = "Transaction failed")
            }
    )
    @PostMapping
    public ResponseEntity<TransactionResponseDto> createTransaction(
            @Valid @RequestBody TransactionRequest request
    ) {
        return ResponseEntity.ok(transactionService.createTransaction(request));
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

    @GetMapping("/month")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsForMonth(
            @RequestParam String accountNumber,
            @RequestParam int year,
            @RequestParam int month) {

        List<TransactionResponseDto> transactions = transactionService.getTransactionsForMonth(accountNumber, year, month);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByStatus(@PathVariable String status) {
        TransactionStatus transactionStatus;
        try {
            transactionStatus = TransactionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        List<TransactionResponseDto> transactions = transactionService.getTransactionsByStatus(transactionStatus);
        return ResponseEntity.ok(transactions);
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
    @GetMapping("/summary/daily")
    public ResponseEntity<List<Map<String, Object>>> getDailyTransactionSummary() {
        return ResponseEntity.ok(transactionService.getDailyTransactionSummary());
    }


    @GetMapping("/summary/account/{accountId}")
    public ResponseEntity<Map<String, BigDecimal>> getAccountSummary(@PathVariable String accountNumber) {
        return ResponseEntity.ok(transactionService.getAccountSummary(accountNumber));
    }

    @GetMapping("/charges/{transactionId}")
    public ResponseEntity<Map<String, Object>> getTransactionCharges(@PathVariable String transactionId) {
        return ResponseEntity.ok(transactionService.getTransactionCharges(transactionId));
    }

    @PostMapping("/statements/send-transaction-statement")
    public ResponseEntity<String> sendStatement(@RequestParam String accountNumber) {
        try {
            transactionService.sendStatement(accountNumber);
            return ResponseEntity.ok("Statement email request sent.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed: " + e.getMessage());
        }
    }
}