package com.bms.loan.dto.request.car;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Year;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarLoanDetailsDto {

    @NotBlank(message = "Car model is required")
    @Size(min = 4, max = 30, message = "Car model must be between 2 and 50 characters")
    private String carModel;

    @NotBlank(message = "Manufacturer is required")
    @Size(min = 2, max = 30, message = "Manufacturer must be between 2 and 50 characters")
    private String manufacturer;

    @Min(value = 1980, message = "Manufacture year must not be before 1980")
    @Max(value = 2025, message = "Manufacture year cannot be in the future")
    private int manufactureYear;

    @NotNull(message = "Car value is required")
    @DecimalMin(value = "50000.00", inclusive = true, message = "Car value must be at least ₹50,000")
    @DecimalMax(value = "5000000.00", inclusive = true, message = "Car value cannot exceed ₹50 lakh (5,000,000)")
    private BigDecimal carValue;

    @NotNull(message = "Down payment is required")
    @DecimalMin(value = "10000.00", inclusive = true, message = "Down payment must be at least ₹10,000")
    private BigDecimal downPayment;

    @NotBlank(message = "Car registration number is required")
    @Pattern(
            regexp = "^[A-Z]{2}\\d{2}[A-Z]{2}\\d{4}$",
            message = "Registration number must follow standard format (e.g., MH12AB1234)"
    )
    private String registrationNumber;

    public static class YearConstants {
        public static final int CURRENT_YEAR = Year.now().getValue();
    }
}
