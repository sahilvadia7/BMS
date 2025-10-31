package com.bms.loan.dto.request;

import com.bms.loan.dto.request.car.CarLoanDetailsDto;
import com.bms.loan.dto.request.education.EducationLoanDetailsDto;
import com.bms.loan.dto.request.home.HomeLoanDetailsDto;
import com.bms.loan.enums.EmploymentType;
import com.bms.loan.enums.LoanType;
import jakarta.validation.Valid;
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
public class LoanApplicationRequest {

    @Valid
    @NotNull(message = "Customer details are required")
    private CustomerRegisterRequest customerDetails;

    @Pattern(
            regexp = "^CIF\\d{16}$",
            message = "CIF number must start with 'CIF' followed by exactly 16 digits (e.g., CIF4019160956300145)"
    )
    private String cifNumber;

    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @NotNull(message = "Requested amount is required")
    @DecimalMin(value = "30000.00", inclusive = true, message = "Requested amount must be at least 10,000")
    @DecimalMax(value = "10000000.00", inclusive = true, message = "Requested amount cannot exceed 1 crore (10,000,000)")
    private BigDecimal requestedAmount;

    @NotNull(message = "Requested tenure is required")
    @Min(value = 6, message = "Minimum tenure should be 6 months")
    @Max(value = 300, message = "Maximum tenure can be 300 months (25 years)")
    private Integer requestedTenureMonths;

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;

    @PositiveOrZero
    private BigDecimal monthlyIncome;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    private String bankAccount;

    @NotBlank(message = "IFSC code is required")
    private String ifscCode;

    @Valid
    private CarLoanDetailsDto carDetails;

    @Valid
    private HomeLoanDetailsDto homeDetails;

    @Valid
    private EducationLoanDetailsDto educationDetails;
}
