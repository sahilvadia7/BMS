package com.bms.loan.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarLoanEvaluationRequestDto {
    private BigDecimal downPayment;
    private boolean insuranceValid;
    private int carConditionScore;
    private int employmentStabilityYears;
}
