package com.bms.account.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponseDTO(
        Long id,
        String accountNumber,
        String accountType,
        BigDecimal balance,
        String status,
        String cifNumber,
        Long branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
