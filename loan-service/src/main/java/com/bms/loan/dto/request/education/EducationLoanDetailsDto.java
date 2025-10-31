package com.bms.loan.dto.request.education;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationLoanDetailsDto {

    @NotBlank(message = "Course name is required")
    @Size(min = 2, max = 100, message = "Course name must be between 2 and 100 characters")
    private String courseName;

    @NotBlank(message = "Field of study is required")
    @Size(min = 2, max = 100, message = "Field of study must be between 2 and 100 characters")
    private String fieldOfStudy;

    @NotBlank(message = "University name is required")
    @Size(min = 2, max = 150, message = "University name must be between 2 and 150 characters")
    private String university;

    @NotBlank(message = "University country is required")
    private String country;

    @NotNull(message = "Course duration is required")
    @Min(value = 6, message = "Course duration must be at least 6 months")
    @Max(value = 60, message = "Course duration cannot exceed 60 months (5 years)")
    private Integer courseDurationMonths;

    @NotNull(message = "Course start date is required")
    private LocalDate courseStartDate;

    @NotNull(message = "Expected course completion date is required")
    private LocalDate expectedCompletionDate;


    @NotNull(message = "Tuition fees are required")
    @DecimalMin(value = "10000.00", inclusive = true, message = "Tuition fees must be at least ₹10,000")
    @DecimalMax(value = "5000000.00", inclusive = true, message = "Tuition fees cannot exceed ₹50 lakh (5,000,000)")
    private BigDecimal tuitionFees;

    @DecimalMin(value = "0.00", inclusive = true, message = "Living expenses cannot be negative")
    private BigDecimal livingExpenses;

    @DecimalMin(value = "0.00", inclusive = true, message = "Other expenses cannot be negative")
    private BigDecimal otherExpenses;

    // Derived total course cost
    private BigDecimal totalCourseCost;


    @NotBlank(message = "Co-applicant name is required")
    @Size(min = 3, max = 100, message = "Co-applicant name must be between 3 and 100 characters")
    private String coApplicantName;

    @NotBlank(message = "Relationship with co-applicant is required")
    private String coApplicantRelation;

    @NotBlank(message = "Co-applicant occupation is required")
    private String coApplicantOccupation;

    @DecimalMin(value = "0.00", inclusive = true, message = "Co-applicant annual income cannot be negative")
    private BigDecimal coApplicantAnnualIncome;


    @NotNull(message = "Moratorium period is required")
    @Min(value = 6, message = "Minimum moratorium is 6 months")
    @Max(value = 12, message = "Maximum moratorium is 12 months")
    private Integer moratoriumMonths;
}
