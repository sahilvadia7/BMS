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
public class CarLoanDetailsDto {
    private String carModel;
    private String manufacturer;
    private int manufactureYear;
    private BigDecimal carValue;
    private String registrationNumber;
}
