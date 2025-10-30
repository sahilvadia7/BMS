package com.bms.account.dtos.accountType;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SavingsAccountDetailsDTO {
    private BigDecimal minimumBalance;
    private Integer withdrawalLimitPerMonth;
    private Boolean chequeBookAvailable;
    private BigDecimal interestRate;
}
