package com.bms.customer.dtos.kyc;

import com.bms.customer.enums.KycStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record KycResponseDTO(
        Long id,
        String documentType,
        String documentNumber,
        String documentUrl,
        String documentFileName,
        KycStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
