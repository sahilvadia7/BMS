package com.bms.account.dtos;

import com.bms.account.enums.AccountStatus;
import com.bms.account.enums.AccountType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record AccountResponseDTO(
        Long id,
        String accountNumber,      // system-generated
        AccountType accountType,
        BigDecimal balance,
//        String currency,
        AccountStatus status,
        Long customerId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
