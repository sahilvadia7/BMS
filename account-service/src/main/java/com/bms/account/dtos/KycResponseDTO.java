package com.bms.account.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KycResponseDTO {

    private Long id;
    private String documentType;
    private String documentNumber;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
