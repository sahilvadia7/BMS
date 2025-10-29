package com.bms.account.dtos.accountType;

import com.bms.account.dtos.BaseAccountRequestDTO;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class SavingsAccountRequestDTO extends BaseAccountRequestDTO {

    @NotNull
    private BigDecimal minimumBalance;

    @NotNull
    private BigDecimal interestRate;

    @NotNull
    private Integer withdrawalLimitPerMonth;

    @NotNull
    private Boolean chequeBookAvailable;
}
