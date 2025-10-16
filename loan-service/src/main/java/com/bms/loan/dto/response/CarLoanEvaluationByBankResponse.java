package com.bms.loan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarLoanEvaluationByBankResponse {

    private Long loanId;                   // Parent loan id
    private String carModel;
    private String manufacturer;
    private int manufactureYear;
    private BigDecimal carValue;
    private String registrationNumber;
    private int carAgeYears;
    private BigDecimal downPayment;
    private boolean insuranceValid;
    private int carConditionScore;
    private int employmentStabilityYears;
}
