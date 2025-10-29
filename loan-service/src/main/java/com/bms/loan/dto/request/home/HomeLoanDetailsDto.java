package com.bms.loan.dto.request.home;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeLoanDetailsDto {
    private String propertyAddress;
    private BigDecimal propertyValue;
    private String builderName;
    private BigDecimal downPayment;
    private String propertyType; // FLAT, VILLA, PLOT
    private String ownershipType; // SELF_OWNED / JOINT
    private String registrationNumber; // property registration id

}
