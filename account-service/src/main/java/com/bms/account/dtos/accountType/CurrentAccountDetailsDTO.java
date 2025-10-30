package com.bms.account.dtos.accountType;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CurrentAccountDetailsDTO {

    private String businessName;
    private BigDecimal overdraftLimit;
    private BigDecimal monthlyServiceCharge;
    private Boolean chequeBookAvailable;
    private Boolean hasOverdraftFacility;
}
