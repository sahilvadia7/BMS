package com.bms.transaction.controller;

import com.bms.transaction.dto.request.PaymentRequest;
import com.bms.transaction.dto.request.SearchTransactionsRequest;
import com.bms.transaction.dto.request.TransactionRequest;
import com.bms.transaction.dto.response.PaymentResponse;
import com.bms.transaction.dto.response.TransactionResponseDto;
import com.bms.transaction.enums.TransactionStatus;
import com.bms.transaction.service.InternalTransactionService;
import com.bms.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transaction Management", description = "Endpoints for processing and retrieving transactions")
@AllArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final InternalTransactionService internalTransactionService;


    /**
     * General purpose endpoint for all types of transactions
     * Handles Deposit, Withdrawal, Transfer, Loan Disbursement, EMI Deduction, and
     * Refund
     */
    @Operation(summary = "Create a transaction (Deposit, Withdrawal, Transfer, Loan, EMI, Refund)", description = "Access: Customer, Internal. Handles all transaction types in one unified API endpoint.", responses = {
            @ApiResponse(responseCode = "200", description = "Transaction processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or insufficient funds"),
            @ApiResponse(responseCode = "404", description = "Account or destination not found"),
            @ApiResponse(responseCode = "500", description = "Transaction failed")
    })
    @PostMapping
    public ResponseEntity<?> createTransaction(@Valid @RequestBody TransactionRequest request) {
        Object result = internalTransactionService.createTransaction(request);

        if (result instanceof TransactionResponseDto) {
            return ResponseEntity.ok(result);
        } else if (result instanceof PaymentResponse) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected transaction result"));
        }
    }


    @Operation(summary = "Get transaction by ID", description = "Access: Customer, Admin", responses = {
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDto> getTransactionById(
            @PathVariable Long transactionId) {
        return ResponseEntity.ok(transactionService.getTransactionById(transactionId));
    }

    @Operation(summary = "Get transactions for a month", description = "Access: Customer, Admin")
    @GetMapping("/month")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsForMonth(
            @RequestParam String accountNumber) {

        List<TransactionResponseDto> transactions = transactionService.getLatestMonthTransactions(accountNumber);
        return ResponseEntity.ok(transactions);
    }

    @Operation(summary = "Get transactions by status", description = "Access: Admin")
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

    @Operation(summary = "Search transactions", description = "Access: Admin", responses = {
            @ApiResponse(responseCode = "200", description = "List of transactions"),
            @ApiResponse(responseCode = "400", description = "Invalid search criteria")
    })
    @GetMapping("/search")
    public ResponseEntity<List<TransactionResponseDto>> searchTransactions(
            @Valid @ModelAttribute SearchTransactionsRequest request) {
        return ResponseEntity.ok(transactionService.searchTransactions(request));
    }

    @Operation(summary = "Get daily transaction summary", description = "Access: Admin", responses = {
            @ApiResponse(responseCode = "200", description = "Transaction summary"),
            @ApiResponse(responseCode = "404", description = "Account or branch not found")
    })
    @GetMapping("/summary/daily")
    public ResponseEntity<List<Map<String, Object>>> getDailyTransactionSummary() {
        return ResponseEntity.ok(transactionService.getDailyTransactionSummary());
    }

    @Operation(summary = "Get account summary", description = "Access: Customer, Admin")
    @GetMapping("/summary/account/{accountNumber}")
    public ResponseEntity<Map<String, BigDecimal>> getAccountSummary(@PathVariable String accountNumber) {
        return ResponseEntity.ok(transactionService.getAccountSummary(accountNumber));
    }

    @Operation(summary = "Get transaction charges", description = "Access: Customer, Admin")
    @GetMapping("/charges/{transactionId}")
    public ResponseEntity<Map<String, Object>> getTransactionCharges(@PathVariable String transactionId) {
        return ResponseEntity.ok(transactionService.getTransactionCharges(transactionId));
    }

    @Operation(summary = "Send transaction statement", description = "Access: Customer")
    @GetMapping("/statements/send-transaction-statement")
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