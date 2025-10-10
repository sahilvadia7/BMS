package com.bms.account.dtos;

import com.bms.account.enums.AccountType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountRequestDTO(
        @NotNull(message = "Account type is required")
        AccountType accountType,

        @NotNull(message = "Balance is required")
        BigDecimal balance,

        @NotNull(message = "Customer ID is required")
        Long customerId,

        @NotNull(message = "Branch ID is required")
        Long branchId
) { }
