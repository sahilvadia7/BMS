package com.bms.transaction.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateTransactionRequest(
        @NotNull(message = "Account ID is required")
        Long accountId,

        @NotNull(message = "Branch ID is required")
        Long branchId,

        @NotNull(message = "Transaction type is required")
        @Pattern(regexp = "DEPOSIT|WITHDRAWAL|TRANSFER", message = "Invalid transaction type")
        String transactionType,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        BigDecimal amount,

        String description,

        String referenceId
) {}