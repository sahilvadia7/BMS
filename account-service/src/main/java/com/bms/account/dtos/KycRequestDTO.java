package com.bms.account.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycRequestDTO {

    @NotBlank(message = "Document type is required")
    String documentType;

    @NotBlank(message = "Document number is required")
    String documentNumber;

//    @NotNull(message = "Status is required for the request model")
//    String status;
}
