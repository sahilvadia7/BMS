package com.bms.transaction.dto.response;

import java.math.BigDecimal;

public record TransactionSummaryDto(
        Long accountId,
        Long branchId,
        int transactionCount,
        BigDecimal totalDeposits,
        BigDecimal totalWithdrawals,
        BigDecimal totalTransfers
) {}
