package com.bms.customer.dtos;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CustomerRequestDTO(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Name is required")
        String name,

        @NotNull(message = "Address is required")
        String address,

        @NotNull(message = "DOB is required")
        LocalDate dob,

        @NotNull(message = "KycId is required")
        Long kycId
) {}
