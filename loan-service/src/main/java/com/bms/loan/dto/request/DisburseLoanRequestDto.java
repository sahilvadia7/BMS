package com.bms.loan.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisburseLoanRequestDto {
    @NotNull
    @Positive
    private BigDecimal disbursedAmount;

    private LocalDateTime disbursedDate;

}
