package com.bms.transaction.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponseDto(
        Long accountId,
		Long destinationAccountId,
        String transactionType,
        BigDecimal amount,
        LocalDateTime transactionDate,
		String currency,
		String channel,
        String status,
        String description,
        String referenceId
) {}