package com.bms.creditscore.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditCheckRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Loan amount is required")
    @Positive(message = "Loan amount must be greater than zero")
    private Double loanAmount;

    @NotNull(message = "Loan type is required")
    private String loanType;
}


