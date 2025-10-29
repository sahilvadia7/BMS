package com.bms.account.dtos;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class KycUploadRequest {

    private Long customerId;
    @Valid
    private KycRequestDTO kyc;
}
