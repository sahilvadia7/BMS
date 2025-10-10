package com.bms.loan.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentRequestDto {
    @NotNull
    @Positive
    private BigDecimal amount;

    private String paymentMode; // UPI, Netbanking, ECS

    private String txnRef;
}
