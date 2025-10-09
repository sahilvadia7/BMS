package com.bms.branch.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record BranchRequestDto(

        @NotBlank(message = "Branch code is required")
        String branchCode,

        @NotBlank(message = "Branch name is required")
        String branchName,

        @NotBlank(message = "IFSC code is required")
        @Size(min = 11, max = 11, message = "IFSC code must be 11 characters")
        String ifscCode,

        @Email(message = "Email should be valid")
        String email,

        @Pattern(regexp = "\\d{10}", message = "Contact number must be 10 digits")
        String contactNumber,

        @NotNull(message = "Branch status is required")
        Boolean status,

        @NotNull(message = "Opening date is required")
        LocalDate openingDate,

        @NotNull(message = "Address is required")
        AddressRequestDto address

) {}
