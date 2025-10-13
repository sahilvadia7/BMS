package com.bms.transaction.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponseDto(
        Long id,
        Long accountId,
        Long branchId,
        String transactionType,
        BigDecimal amount,
        LocalDateTime transactionDate,
        String status,
        String description,
        String referenceId
) {}