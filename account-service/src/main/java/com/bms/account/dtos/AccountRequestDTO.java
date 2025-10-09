package com.bms.account.dtos;

import com.bms.account.enums.AccountType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountRequestDTO(
        @NotNull(message = "Account type is required")
        AccountType accountType,

        @NotNull(message = "Balance is required")
        BigDecimal balance,

//        String currency, // Optional, can be null

        @NotNull(message = "Customer ID is required")
        Long customerId
) { }
