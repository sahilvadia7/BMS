package com.bms.customer.dtos.kyc;

import com.bms.customer.enums.KycStatus;
import java.time.LocalDateTime;

public record KycResponseDTO(
        Long id,
        String documentType,
        String documentNumber,
        KycStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
