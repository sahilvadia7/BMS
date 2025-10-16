package com.bms.account.dtos.accountType;

import java.math.BigDecimal;

public record AccountTypeUpdateDto(
        String type,       // e.g. SAVINGS, CURRENT, FIXED_DEPOSIT
        BigDecimal interest, // Updated interest rate
        Boolean active
) {
}
