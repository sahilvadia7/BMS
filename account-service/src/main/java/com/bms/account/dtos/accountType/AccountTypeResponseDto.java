package com.bms.account.dtos.accountType;

import com.bms.account.constant.AccountTypeEnum;

import java.math.BigDecimal;

public record AccountTypeResponseDto(
        Long id,
        AccountTypeEnum type,
        BigDecimal interest,
        Boolean active
) {
}
