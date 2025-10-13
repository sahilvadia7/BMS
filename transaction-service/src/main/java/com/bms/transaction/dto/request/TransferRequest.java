package com.bms.transaction.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record TransferRequest(
        @NotNull(message = "From account ID is required")
        Long fromAccountId,

        @NotNull(message = "To account ID is required")
        Long toAccountId,

        @NotNull(message = "Branch ID is required")
        Long branchId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        BigDecimal amount,

        String description
) {}