package com.bms.customer.dtos.response;

import com.bms.customer.enums.KycStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerKycMappingDTO {

    private Long kycId;
    private String documentType;
    private String documentNumber;
    private KycStatus documentStatus;
    private boolean isPrimary;
}