package com.bms.transaction.dto.response;

import java.math.BigDecimal;

public record TransactionSummaryDto(
        String accountNumber,
        Long branchId,
        int transactionCount,
        BigDecimal totalDeposits,
        BigDecimal totalWithdrawals,
        BigDecimal totalTransfers
) {}
