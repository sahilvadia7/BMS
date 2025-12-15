package com.bms.loan.dto.request.transaction;

import com.bms.loan.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {

    @NotBlank(message = "initiatedBy is required")
    private String initiatedBy;

    @NotBlank(message = "accountNumber is required")
    private String accountNumber;

    private String destinationAccountNumber;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "currency is required")
    private Currency currency;

    @NotNull(message = "transactionType is required")
    private String transactionType;

    private String description;

    private String pin;

    @NotBlank(message = "idempotencyKey is required")
    private String idempotencyKey;
}
