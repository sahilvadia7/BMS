package com.bms.loan.dto.response;

import com.bms.loan.enums.KycStatus;
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
