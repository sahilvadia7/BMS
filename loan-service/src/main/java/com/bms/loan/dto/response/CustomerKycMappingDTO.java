package com.bms.loan.dto.response;

import com.bms.loan.enums.KycStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CustomerKycMappingDTO {

    private Long kycId;
    private String documentType;
    private String documentNumber;
    private KycStatus documentStatus;
    private LocalDate approvalDate;
    private boolean isPrimary;
}
