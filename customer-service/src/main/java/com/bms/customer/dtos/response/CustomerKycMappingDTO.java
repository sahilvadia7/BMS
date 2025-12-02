package com.bms.customer.dtos.response;

import com.bms.customer.enums.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerKycMappingDTO {

    private Long kycId;
    private String documentType;
    private String documentNumber;
    private KycStatus documentStatus;
    private LocalDate approvalDate;
    private boolean isPrimary;
}