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
public class ApproveLoanRequestDto {
    @NotNull
    @Positive
    private BigDecimal approvedAmount;

    @Positive
    private BigDecimal approvedRate;

    private Integer approvedTenure;

    private String approvedBy;

}
