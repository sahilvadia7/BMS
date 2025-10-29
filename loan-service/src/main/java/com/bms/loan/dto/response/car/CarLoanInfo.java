package com.bms.loan.dto.response.car;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarLoanInfo {

    private String carModel;
    private String manufacturer;
    private int manufactureYear;
    private BigDecimal carValue;
    private String registrationNumber;
    private int carAgeYears;
    private int carConditionScore;
    private BigDecimal downPayment;
    private boolean insuranceValid;
    private int employmentStabilityYears;
}
