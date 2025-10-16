package com.bms.account.dtos.accountType;

import com.bms.account.constant.AccountTypeEnum;

import java.math.BigDecimal;

public record AccountTypeRequestDto(
        AccountTypeEnum type,
        BigDecimal interest,
        Boolean active
) {
}
