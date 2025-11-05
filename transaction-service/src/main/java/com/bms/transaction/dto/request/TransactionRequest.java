package com.bms.transaction.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record TransactionRequest(

        @NotNull(message = "Account ID is required")
        @Positive(message = "Account ID must be positive")
        Long accountId,

        Long destinationAccountId,

        @NotNull(message = "Transaction type is required")
        @Pattern(regexp = "DEPOSIT|WITHDRAWAL|TRANSFER", message = "Invalid transaction type")
        String transactionType,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.00", message = "Amount must be at least 1.00")
        @DecimalMax(value = "10000000.00", message = "Amount exceeds transaction limit")
        BigDecimal amount,

        @NotNull(message = "Currency is required")
        @Pattern(regexp = "INR|USD|EUR", message = "Invalid currency")
        String currency,

        @NotNull(message = "Channel is required")
        @Pattern(regexp = "ONLINE|BRANCH|ATM", message = "Invalid channel")
        String channel,

        @NotNull(message = "PIN is required")
        @Pattern(regexp = "\\d{4}", message = "PIN must be a 4-digit number")
        String pin,

        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description
) {}
