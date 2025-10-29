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
public class CurrentAccountRequestDTO extends BaseAccountRequestDTO {

    @NotBlank
    private String businessName;

    @NotNull
    private BigDecimal overdraftLimit;

    @NotNull
    private BigDecimal monthlyServiceCharge;

    @NotNull
    private Boolean hasOverdraftFacility;

    @NotNull
    private Boolean chequeBookAvailable;
}
