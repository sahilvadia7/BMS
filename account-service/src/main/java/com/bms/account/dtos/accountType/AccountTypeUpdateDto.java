package com.bms.account.dtos.accountType;

import java.math.BigDecimal;

public record AccountTypeUpdateDto(
        String type,       // e.g., SAVINGS, CURRENT
        BigDecimal interest,
        Boolean active
) {
}
