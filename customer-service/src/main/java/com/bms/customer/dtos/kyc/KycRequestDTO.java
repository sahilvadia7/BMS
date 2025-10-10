package com.bms.customer.dtos.kyc;

import com.bms.customer.enums.KycStatus;
import jakarta.validation.constraints.NotBlank;

public record KycRequestDTO(

        @NotBlank(message = "Document type is required")
        String documentType,

        @NotBlank(message = "Document number is required")
        String documentNumber,

        KycStatus status // optional in create, default will be PENDING
) { }
