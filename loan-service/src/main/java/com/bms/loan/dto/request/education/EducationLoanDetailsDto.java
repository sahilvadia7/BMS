package com.bms.loan.dto.request.education;

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
public class EducationLoanDetailsDto {

    @NotBlank(message = "Course name is required")
    @Size(min = 2, max = 100, message = "Course name must be between 2 and 100 characters")
    private String courseName;

    @NotBlank(message = "University name is required")
    @Size(min = 2, max = 150, message = "University name must be between 2 and 150 characters")
    private String university;

    @NotNull(message = "Course duration is required")
    @Min(value = 6, message = "Course duration must be at least 6 months")
    @Max(value = 36, message = "Course duration cannot exceed 36 months (3 years)")
    private Integer courseDurationMonths;

    @NotNull(message = "Tuition fees are required")
    @DecimalMin(value = "10000.00", inclusive = true, message = "Tuition fees must be at least ₹10,000")
    @DecimalMax(value = "5000000.00", inclusive = true, message = "Tuition fees cannot exceed ₹50 lakh (5,000,000)")
    private BigDecimal tuitionFees;

    @NotBlank(message = "Co-applicant name is required")
    @Size(min = 3, max = 100, message = "Co-applicant name must be between 2 and 100 characters")
    private String coApplicantName;
}
