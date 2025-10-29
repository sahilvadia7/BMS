package com.bms.customer.dtos.kyc;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class KycUploadRequestDto {

    private Long customerId;
    @Valid
    private KycRequestDTO kyc;
}
