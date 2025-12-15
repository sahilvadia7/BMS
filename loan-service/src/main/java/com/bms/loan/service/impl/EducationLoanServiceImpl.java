package com.bms.loan.service.impl;

import com.bms.loan.Repository.education.EducationLoanRepository;
import com.bms.loan.Repository.InterestRateRepository;
import com.bms.loan.Repository.LoanRepository;
import com.bms.loan.Repository.education.EducationVerificationReportRepository;
import com.bms.loan.dto.response.loan.LoanEvaluationResponse;
import com.bms.loan.entity.InterestRate;
import com.bms.loan.entity.education.EducationLoanDetails;
import com.bms.loan.entity.education.EducationVerificationReport;
import com.bms.loan.entity.loan.Loans;
import com.bms.loan.enums.EmploymentType;
import com.bms.loan.enums.LoanStatus;
import com.bms.loan.enums.RiskBand;
import com.bms.loan.exception.InvalidLoanStatusException;
import com.bms.loan.exception.ResourceNotFoundException;
import com.bms.loan.service.EducationLoanService;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class EducationLoanServiceImpl implements EducationLoanService {

    private final LoanRepository loanRepository;
    private final EducationLoanRepository educationLoanRepository;
    private final InterestRateRepository interestRateRepository;
    private final EducationVerificationReportRepository verificationReportRepository;
    public EducationLoanServiceImpl(LoanRepository loanRepository,
                                    EducationLoanRepository educationLoanRepository,
                                    InterestRateRepository interestRateRepository,
                                    EducationVerificationReportRepository verificationReportRepository) {
                this.loanRepository = loanRepository;
                this.educationLoanRepository = educationLoanRepository;
                this.interestRateRepository = interestRateRepository;
                this.verificationReportRepository = verificationReportRepository;
    }

        @Override
        public LoanEvaluationResponse evaluateLoan(Long loanId) {
            Loans loan = loanRepository.findById(loanId)
                    .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

            if (loan.getStatus() != LoanStatus.VERIFIED) {
                throw new InvalidLoanStatusException(
                        "Loan must be Verified before Evaluating the Education Loan");
            }

            EducationVerificationReport verify = verificationReportRepository
                    .findByLoans_LoanId(loanId)
                    .orElseThrow(() -> new EntityNotFoundException("Verification report not found"));

            EducationLoanDetails edu = educationLoanRepository.findByLoans_LoanId(loanId)
                    .orElseThrow(() -> new EntityNotFoundException("Education loan details not found"));

            InterestRate rate = interestRateRepository.findByLoanType(String.valueOf(loan.getLoanType()))
                    .orElseThrow(() -> new ResourceNotFoundException(
                                                "Interest Rate not found with Type: " + loan.getLoanType()));

            // HARD REJECTION
            if (!verify.isAdmissionVerified()
                    || !verify.isCollegeRecognized()
                    || !verify.isFeeStructureVerified()
                    || !verify.isStudentBackgroundClear()
                    || !verify.isCoApplicantIdentityValid()) {

                loan.setStatus(LoanStatus.REJECTED);
                loan.setRemarks("Rejected due to verification failure");
                loanRepository.save(loan);

                return LoanEvaluationResponse.builder()
                        .loanId(loan.getLoanId())
                        .loanType(loan.getLoanType().name())
                        .approved(false)
                        .status(loan.getStatus().name())
                        .remarks(loan.getRemarks())
                        .build();
            }

            // RISK SCORING
            int score = 0;

            // A. Education Risk (40)
            if ("India".equalsIgnoreCase(edu.getCountry()))
                score += 15;
            else
                score += 10;

            if (edu.getFieldOfStudy().matches("Engineering|Medical|MBA|Computer Science"))
                score += 15;
            else
                score += 5;

            if (edu.getCourseDurationMonths() <= 48)
                score += 10;


            // B. Financial Risk (30)
            BigDecimal income = edu.getCoApplicantAnnualIncome();
            if (income.compareTo(BigDecimal.valueOf(600000)) >= 0)
                score += 15;
            else if (income.compareTo(BigDecimal.valueOf(300000)) >= 0)
                score += 10;

            BigDecimal ltv = loan.getRequestedAmount()
                    .divide(edu.getTotalCourseCost(), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            if (ltv.compareTo(BigDecimal.valueOf(90)) <= 0)
                score += 15;
            else if (ltv.compareTo(BigDecimal.valueOf(100)) <= 0)
                score += 10;

            // C. Credit Risk (20)
            int cibil = loan.getExternalCreditScore();
            if (cibil >= 750)
                score += 20;
            else if (cibil >= 700)
                score += 10;


            // D. Structural Risk (10)
            if (edu.getMoratoriumMonths() <= 12)
                score += 10;


            // DECISION

            RiskBand band;
            BigDecimal finalRate = rate.getBaseRate();
            BigDecimal approvedAmount = loan.getRequestedAmount();

            if (score >= 75) {
                band = RiskBand.LOW;
            } else if (score >= 60) {
                band = RiskBand.MEDIUM;
                finalRate = finalRate.add(BigDecimal.valueOf(0.5));
            } else if (score >= 50) {
                band = RiskBand.HIGH;
                approvedAmount = approvedAmount.multiply(BigDecimal.valueOf(0.75));
                finalRate = finalRate.add(BigDecimal.valueOf(1.0));
            } else {
                band = RiskBand.VERY_HIGH;
            }

            if (band == RiskBand.VERY_HIGH) {
                loan.setStatus(LoanStatus.REJECTED);
                loan.setRemarks("Rejected due to high risk score: " + score);
            } else {
                loan.setStatus(LoanStatus.EVALUATED);
                loan.setApprovedAmount(approvedAmount);
                loan.setInterestRate(finalRate);
                loan.setRemarks("Approved under " + band + " risk (" + score + ")");
            }

            loanRepository.save(loan);

            return LoanEvaluationResponse.builder()
                    .loanId(loanId)
                    .approved(band != RiskBand.VERY_HIGH)
                    .status(loan.getStatus().name())
                    .remarks(loan.getRemarks())
                    .build();

        }
}
