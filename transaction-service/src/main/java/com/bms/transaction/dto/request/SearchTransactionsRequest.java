package com.bms.transaction.dto.request;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record SearchTransactionsRequest(
        Long accountId,
        Long branchId,
        @Pattern(regexp = "DEPOSIT|WITHDRAWAL|TRANSFER", message = "Invalid transaction type")
        String transactionType,
        @Pattern(regexp = "PENDING|COMPLETED|FAILED", message = "Invalid status")
        String status,
        @PastOrPresent(message = "Start date must be in the past or present")
        LocalDateTime startDate,
        @PastOrPresent(message = "End date must be in the past or present")
        LocalDateTime endDate
) {}