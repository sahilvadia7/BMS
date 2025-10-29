package com.bms.account.dtos;

import com.bms.account.constant.AccountStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountRequestDTO {

        @NotNull(message = "Account type ID is required")
        private String accountType; // e.g. SAVINGS, CURRENT, etc.

        @NotNull(message = "Customer ID is required")
        private String cifNumber;

//        @NotNull(message = "Branch ID is required")
//        private Long branchId;

        @DecimalMin(value = "0.0", inclusive = true, message = "Balance cannot be negative")
        private BigDecimal initialDeposit;

//        private AccountStatus status; // optional (default = PENDING)

        // Nested KYC details
        @NotNull(message = "KYC details are required")
        private KycRequestDTO kycDetails;
}
