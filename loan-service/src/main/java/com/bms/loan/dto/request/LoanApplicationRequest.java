package com.bms.loan.dto.request;

import com.bms.loan.dto.request.car.CarLoanDetailsDto;
import com.bms.loan.dto.request.education.EducationLoanDetailsDto;
import com.bms.loan.dto.request.home.HomeLoanDetailsDto;
import com.bms.loan.enums.EmploymentType;
import com.bms.loan.enums.LoanType;
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

    private CustomerRegisterRequest customerDetails;
    private String cifNumber;
    private LoanType loanType;
    private BigDecimal requestedAmount;
    private Integer requestedTenureMonths;
    private EmploymentType employmentType;
    private BigDecimal monthlyIncome;
    private String bankName;
    private String bankAccount;
    private String ifscCode;

    private CarLoanDetailsDto carDetails;
    private HomeLoanDetailsDto homeDetails;
    private EducationLoanDetailsDto educationDetails;
}
