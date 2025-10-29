package com.bms.customer.dtos.kyc;

import com.bms.customer.enums.KycStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record KycRequestDTO(

        @NotBlank(message = "Document type is required")
        String documentType,

        @NotBlank(message = "Document number is required")
        String documentNumber

//        @NotNull(message = "Status is required for the request model")
//        KycStatus status
) { }
