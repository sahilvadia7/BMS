package com.bms.loan.dto.request.home;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeLoanDetailsDto {

    @NotBlank(message = "Property address is required")
    @Size(min = 10, max = 255, message = "Property address must be between 10 and 255 characters")
    private String propertyAddress;

    @NotNull(message = "Property value is required")
    @DecimalMin(value = "100000.00", inclusive = true, message = "Property value must be at least ₹1,00,000")
    @DecimalMax(value = "100000000.00", inclusive = true, message = "Property value cannot exceed ₹10 crore (100,000,000)")
    private BigDecimal propertyValue;

    @NotBlank(message = "Builder name is required")
    @Size(min = 3, max = 100, message = "Builder name must be between 2 and 100 characters")
    private String builderName;

    @NotNull(message = "Down payment is required")
    @DecimalMin(value = "50000.00", inclusive = true, message = "Down payment must be at least ₹50,000")
    private BigDecimal downPayment;

    @NotBlank(message = "Property type is required")
    @Pattern(
            regexp = "^(FLAT|VILLA|PLOT)$",
            message = "Property type must be one of: FLAT, VILLA, or PLOT"
    )
    private String propertyType; // FLAT, VILLA, PLOT

    @NotBlank(message = "Ownership type is required")
    @Pattern(
            regexp = "^(SELF_OWNED|JOINT)$",
            message = "Ownership type must be either SELF_OWNED or JOINT"
    )
    private String ownershipType; // SELF_OWNED / JOINT

    @NotBlank(message = "Property registration number is required")
    @Pattern(
            regexp = "^[A-Z0-9-]{5,20}$",
            message = "Invalid registration number format (allowed: A-Z, 0-9, '-')"
    )
    private String registrationNumber; // property registration id

}
