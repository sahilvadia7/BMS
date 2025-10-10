package com.bms.account.dtos;

import com.bms.account.enums.AccountStatus;
import com.bms.account.enums.AccountType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponseDTO(
        Long id,
        String accountNumber,
        AccountType accountType,
        BigDecimal balance,
        AccountStatus status,
        Long customerId,
        Long branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
)  { }
