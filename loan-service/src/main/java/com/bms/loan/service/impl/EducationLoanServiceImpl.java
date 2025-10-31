package com.bms.loan.service.impl;

import com.bms.loan.Repository.EducationLoanRepository;
import com.bms.loan.Repository.InterestRateRepository;
import com.bms.loan.Repository.LoanRepository;
import com.bms.loan.dto.response.loan.LoanEvaluationResponse;
import com.bms.loan.entity.InterestRate;
import com.bms.loan.entity.education.EducationLoanDetails;
import com.bms.loan.entity.loan.Loans;
import com.bms.loan.enums.EmploymentType;
import com.bms.loan.enums.LoanStatus;
import com.bms.loan.exception.InvalidLoanStatusException;
import com.bms.loan.exception.ResourceNotFoundException;
import com.bms.loan.service.EducationLoanService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class EducationLoanServiceImpl implements EducationLoanService {

    private final LoanRepository loanRepository;
    private final EducationLoanRepository educationLoanRepository;
    private final InterestRateRepository interestRateRepository;

    @Override
    public LoanEvaluationResponse evaluateLoan(Long loanId) {
        Loans loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.VERIFIED) {
            throw new InvalidLoanStatusException("Loan must be Verified before Evaluating the Education Loan");
        }

        EducationLoanDetails edu = educationLoanRepository.findByLoans_LoanId(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Education loan details not found"));

        InterestRate rate = interestRateRepository.findByLoanType(String.valueOf(loan.getLoanType()))
                .orElseThrow(() -> new ResourceNotFoundException("Interest Rate not found with Type: " + loan.getLoanType()));

        // Extract data
        BigDecimal requestedAmount = loan.getRequestedAmount() != null ? loan.getRequestedAmount() : BigDecimal.ZERO;
        BigDecimal totalCourseCost = edu.getTotalCourseCost() != null ? edu.getTotalCourseCost() : BigDecimal.ZERO;
        BigDecimal coApplicantIncome = edu.getCoApplicantAnnualIncome() != null ? edu.getCoApplicantAnnualIncome() : BigDecimal.ZERO;
        BigDecimal monthlyIncome = loan.getMonthlyIncome() != null ? loan.getMonthlyIncome() : BigDecimal.ZERO;
        Integer moratorium = edu.getMoratoriumMonths() != null ? edu.getMoratoriumMonths() : 12;
        int tenure = loan.getRequestedTenureMonths() != null ? loan.getRequestedTenureMonths() : 60;
        int creditScore = loan.getExternalCreditScore();
        boolean docsVerified = edu.isVerified();

        // Calculate LTV
        BigDecimal ltv = totalCourseCost.compareTo(BigDecimal.ZERO) > 0
                ? requestedAmount.divide(totalCourseCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.valueOf(100);

        // Start scoring
        int totalPoints = 0;

        // 1. Document verification
        if (docsVerified) totalPoints += 10;

        // 2. LTV (loan-to-value)
        if (ltv.compareTo(BigDecimal.valueOf(90)) <= 0) totalPoints += 25;
        else if (ltv.compareTo(BigDecimal.valueOf(100)) <= 0) totalPoints += 10;

        // 3. Co-applicant income
        if (coApplicantIncome.compareTo(BigDecimal.valueOf(300000)) >= 0) totalPoints += 20;
        else if (coApplicantIncome.compareTo(BigDecimal.valueOf(100000)) >= 0) totalPoints += 10;

        // 4. Credit score
        if (creditScore >= 750) totalPoints += 20;
        else if (creditScore >= 700) totalPoints += 10;

        // 5. Tenure check
        if (tenure >= rate.getMinTenure() && tenure <= rate.getMaxTenure()) totalPoints += 10;

        // 6. Moratorium
        if (moratorium <= 6) totalPoints += 10;
        else if (moratorium <= 12) totalPoints += 5;

        // 7. Course fee coverage
        BigDecimal courseTotal = edu.getTuitionFees()
                .add(edu.getLivingExpenses() != null ? edu.getLivingExpenses() : BigDecimal.ZERO)
                .add(edu.getOtherExpenses() != null ? edu.getOtherExpenses() : BigDecimal.ZERO);
        if (courseTotal.compareTo(totalCourseCost) == 0) totalPoints += 10;

        // 8. Employment type
        if (loan.getEmploymentType() == EmploymentType.STUDENT) totalPoints += 5;

        boolean approved;
        String remarks;
        BigDecimal finalInterestRate = rate.getBaseRate();

        if (totalPoints >= 80) {
            approved = true;
            remarks = "Approved: strong academic and financial profile (" + totalPoints + " points)";
        } else if (totalPoints >= 50) {
            approved = true;
            finalInterestRate = finalInterestRate.add(BigDecimal.valueOf(0.5));
            remarks = "Conditionally approved with higher rate (" + totalPoints + " points)";
        } else {
            approved = false;
            remarks = "Rejected: insufficient eligibility (" + totalPoints + " points)";
        }

        // Update loan
        loan.setInterestRate(finalInterestRate);
        loan.setApprovedAmount(approved ? requestedAmount : requestedAmount.multiply(BigDecimal.valueOf(0.8)));
        loan.setStatus(approved ? LoanStatus.EVALUATED : LoanStatus.REJECTED);
        loan.setRemarks(remarks);
        loanRepository.save(loan);

        return LoanEvaluationResponse.builder()
                .loanId(loanId)
                .loanType(loan.getLoanType().name())
                .approved(approved)
                .remarks(remarks)
                .status(loan.getStatus().name())
                .build();    }
}
