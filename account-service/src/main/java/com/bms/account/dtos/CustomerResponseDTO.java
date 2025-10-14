package com.bms.account.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CustomerResponseDTO(
        Long id,
        Long userId,
        String name,
        String address,
        LocalDate dob,
        Long kycId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}
