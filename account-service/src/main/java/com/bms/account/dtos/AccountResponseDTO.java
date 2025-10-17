package com.bms.account.dtos;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record AccountResponseDTO(
        Long id,
        String accountNumber,
        String accountType,
        BigDecimal balance,
        String status,
        Long customerId,
        String cifNumber,
        Long branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
