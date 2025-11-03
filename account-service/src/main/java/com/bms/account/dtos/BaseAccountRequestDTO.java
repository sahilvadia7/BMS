package com.bms.account.dtos;

import com.bms.account.constant.IncomeSourceType;
import com.bms.account.constant.OccupationType;
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

    // 🧩 New fields
    @NotNull(message = "Occupation is required")
    private OccupationType occupationType;

    @NotNull(message = "Source of income is required")
    private IncomeSourceType incomeSourceType;

    @DecimalMin(value = "0.0", message = "Gross annual income must be positive")
    private BigDecimal grossAnnualIncome;

    @NotNull(message = "Nominee details are required")
    private NomineeDTO nominee;
}
