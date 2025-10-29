package com.bms.account.dtos;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BaseAccountRequestDTO {

    @NotNull(message = "Account type is required")
    private String accountType;  // e.g. SAVINGS, CURRENT

    @NotNull(message = "CIF number is required")
    private String cifNumber;

    @DecimalMin(value = "0.0", inclusive = true, message = "Balance cannot be negative")
    private BigDecimal initialDeposit;

    @NotNull(message = "KYC details are required")
    private KycRequestDTO kycDetails;
}
