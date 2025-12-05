package com.bms.loan.service.impl;

import com.bms.loan.Repository.home.HomeLoanRepository;
import com.bms.loan.Repository.LoanSanctionRepository;
import com.bms.loan.Repository.home.HomeVerificationReportRepository;
import com.bms.loan.Repository.InterestRateRepository;
import com.bms.loan.Repository.LoanRepository;
import com.bms.loan.dto.email.SanctionEmailDTO;
import com.bms.loan.dto.request.home.LoanSanctionRequest;
import com.bms.loan.dto.response.CustomerDetailsResponseDTO;
import com.bms.loan.dto.response.home.LoanSanctionResponseDTO;
import com.bms.loan.dto.response.loan.LoanEvaluationResponse;
import com.bms.loan.entity.InterestRate;
import com.bms.loan.entity.home.HomeLoanDetails;
import com.bms.loan.entity.home.LoanSanction;
import com.bms.loan.entity.loan.Loans;
import com.bms.loan.enums.LoanStatus;
import com.bms.loan.exception.InvalidLoanStatusException;
import com.bms.loan.exception.ResourceNotFoundException;
import com.bms.loan.feign.CustomerClient;
import com.bms.loan.feign.NotificationClient;
import com.bms.loan.service.LoanEvolutionAndSanctionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

// Verification → Evaluation → Sanction → Disbursement

@Service
public class HomeLoanServiceImpl implements LoanEvolutionAndSanctionService {

        private final LoanRepository loanRepository;
        private final HomeLoanRepository homeLoanRepository;
        private final InterestRateRepository interestRateRepository;
        private final HomeVerificationReportRepository homeVerificationReportRepository;
        private final LoanSanctionRepository homeLoanSanctionRepository;
        private final NotificationClient notificationClient;
        private final CustomerClient customerClient;

        public HomeLoanServiceImpl(LoanRepository loanRepository,
                        HomeLoanRepository homeLoanRepository,
                        InterestRateRepository interestRateRepository,
                        HomeVerificationReportRepository homeVerificationReportRepository,
                        LoanSanctionRepository homeLoanSanctionRepository,
                        NotificationClient notificationClient,
                        CustomerClient customerClient) {
                this.loanRepository = loanRepository;
                this.homeLoanRepository = homeLoanRepository;
                this.interestRateRepository = interestRateRepository;
                this.homeVerificationReportRepository = homeVerificationReportRepository;
                this.homeLoanSanctionRepository = homeLoanSanctionRepository;
                this.notificationClient = notificationClient;
                this.customerClient = customerClient;
        }


        // Simple EMI calculator
        private BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, int months) {
                BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12 * 100), RoundingMode.HALF_UP);
                BigDecimal numerator = principal.multiply(monthlyRate)
                                .multiply((BigDecimal.ONE.add(monthlyRate)).pow(months));
                BigDecimal denominator = ((BigDecimal.ONE.add(monthlyRate)).pow(months)).subtract(BigDecimal.ONE);
                return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        }

        @Override
        public LoanEvaluationResponse evaluateLoan(Long loanId) {
                Loans loan = loanRepository.findById(loanId)
                                .orElseThrow(() -> new EntityNotFoundException("Loan not found"));

                if (loan.getStatus() != LoanStatus.VERIFIED) {
                        throw new InvalidLoanStatusException("Loan must be Verified before Evaluating the Home Loan");
                }

                HomeLoanDetails details = homeLoanRepository.findByLoans_LoanId(loanId)
                                .orElseThrow(() -> new EntityNotFoundException("Home loan details not found"));

                InterestRate rate = interestRateRepository.findByLoanType(String.valueOf(loan.getLoanType()))
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Interest Rate not found with Type: " + loan.getLoanType()));

                // Extract data safely
                BigDecimal propertyValue = details.getPropertyValue() != null ? details.getPropertyValue()
                                : BigDecimal.ZERO;
                BigDecimal downPayment = details.getDownPayment() != null ? details.getDownPayment() : BigDecimal.ZERO;
                BigDecimal requestedAmount = loan.getRequestedAmount();
                BigDecimal income = loan.getMonthlyIncome() != null ? loan.getMonthlyIncome() : BigDecimal.ZERO;
                ;
                int tenureMonths = loan.getRequestedTenureMonths() != null ? loan.getRequestedTenureMonths() : 240;
                int creditScore = loan.getExternalCreditScore();
                String employmentType = loan.getEmploymentType() != null ? loan.getEmploymentType().name() : "SALARIED";

                // Calculate ratios
                BigDecimal ltv = propertyValue.compareTo(BigDecimal.ZERO) > 0
                                ? requestedAmount.divide(propertyValue, 4, RoundingMode.HALF_UP)
                                                .multiply(BigDecimal.valueOf(100))
                                : BigDecimal.valueOf(100);

                BigDecimal downPaymentPercent = propertyValue.compareTo(BigDecimal.ZERO) > 0
                                ? downPayment.divide(propertyValue, 4, RoundingMode.HALF_UP)
                                                .multiply(BigDecimal.valueOf(100))
                                : BigDecimal.ZERO;

                BigDecimal monthlyEmi = calculateEmi(requestedAmount, rate.getBaseRate(), tenureMonths);
                BigDecimal incomeToEmiRatio = income.compareTo(BigDecimal.ZERO) > 0
                                ? monthlyEmi.divide(income, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                                : BigDecimal.valueOf(100);

                // Assign points per rule
                int creditScorePoints;
                if (creditScore >= 750)
                        creditScorePoints = 25;
                else if (creditScore >= 700)
                        creditScorePoints = 20;
                else if (creditScore >= 650)
                        creditScorePoints = 10;
                else
                        creditScorePoints = 0;

                int ltvPoints;
                if (ltv.compareTo(BigDecimal.valueOf(80)) <= 0)
                        ltvPoints = 25;
                else if (ltv.compareTo(BigDecimal.valueOf(90)) <= 0)
                        ltvPoints = 15;
                else
                        ltvPoints = 5;

                int incomePoints;
                if (incomeToEmiRatio.compareTo(BigDecimal.valueOf(40)) <= 0)
                        incomePoints = 25;
                else if (incomeToEmiRatio.compareTo(BigDecimal.valueOf(50)) <= 0)
                        incomePoints = 15;
                else
                        incomePoints = 5;

                int downPaymentPoints;
                if (downPaymentPercent.compareTo(BigDecimal.valueOf(20)) >= 0)
                        downPaymentPoints = 10;
                else if (downPaymentPercent.compareTo(BigDecimal.valueOf(10)) >= 0)
                        downPaymentPoints = 5;
                else
                        downPaymentPoints = 0;

                int employmentPoints = switch (employmentType.toUpperCase()) {
                        case "SALARIED", "GOVERNMENT", "SELF_EMPLOYED" -> 10;
                        default -> 5;
                };

                int tenurePoints = tenureMonths <= 240 ? 5 : 3;

                // Calculate total score
                int totalScore = creditScorePoints + ltvPoints + incomePoints + downPaymentPoints + employmentPoints
                                + tenurePoints;

                // Determine output
                boolean approved;
                String remarks;
                BigDecimal finalInterestRate = rate.getBaseRate();

                if (totalScore >= 75) {
                        approved = true;
                        remarks = "Approved: strong profile (" + totalScore + " points)";
                } else if (totalScore >= 50) {
                        approved = true;
                        finalInterestRate = finalInterestRate.add(BigDecimal.valueOf(0.5)); // risk-based premium
                        remarks = "Conditionally approved with higher rate (" + totalScore + " points)";
                } else {
                        approved = false;
                        remarks = "Rejected: low evaluation score (" + totalScore + " points)";
                }

                // Update loan record
                loan.setApprovedAmount(approved ? requestedAmount : requestedAmount.multiply(BigDecimal.valueOf(0.8)));
                loan.setInterestRate(finalInterestRate);
                loan.setStatus(approved ? LoanStatus.EVALUATED : LoanStatus.REJECTED);
                loan.setRemarks(remarks);
                loanRepository.save(loan);

                // Build response
                return LoanEvaluationResponse.builder()
                                .loanId(loanId)
                                .loanType(String.valueOf(loan.getLoanType()))
                                .approved(approved)
                                .remarks(remarks)
                                .status(loan.getStatus().name())
                                .build();
        }


        @Transactional
        public LoanSanctionResponseDTO sanctionHomeLoan(Long loanId, LoanSanctionRequest request) {
                Loans loan = loanRepository.findById(loanId)
                                .orElseThrow(() -> new ResourceNotFoundException("Loan not found " + loanId));

                if (loan.getStatus() != LoanStatus.EVALUATED) {
                        throw new InvalidLoanStatusException("Loan must be evaluated before sanctioning");
                }

                CustomerDetailsResponseDTO customer = customerClient.getByCif(loan.getCifNumber());

        BigDecimal principal = request.getSanctionedAmount();
        Integer tenure = request.getTenureMonths();

        BigDecimal emi = calculateEmi(principal, loan.getInterestRate(), tenure);

        // Save sanction
        LoanSanction sanction = LoanSanction.builder()
                .loans(loan)
                .sanctionedAmount(principal)
                .interestRate(loan.getInterestRate())
                .tenureMonths(tenure)
                .sanctionDate(LocalDate.now())
                .eSigned(false)
                .build();

                loan.setStatus(LoanStatus.SANCTIONED);
                homeLoanSanctionRepository.save(sanction);
                loanRepository.save(loan);

        // sanction email to customer
        SanctionEmailDTO email = SanctionEmailDTO.builder()
                .toEmail(customer.getEmail())
                .customerName(customer.getFirstName()+" "+customer.getLastName())
                .loanType(loan.getLoanType().name())
                .sanctionedAmount(principal)
                .interestRate(loan.getInterestRate())
                .tenureMonths(tenure)
                .emiAmount(emi)
                .sanctionDate(LocalDate.now())
                .build();

                notificationClient.sendSanctionEmail(email);

        return LoanSanctionResponseDTO.builder()
                .loanId(loan.getLoanId())
                .sanctionedAmount(principal)
                .interestRate(loan.getInterestRate())
                .tenureMonths(tenure)
                .emiAmount(emi)
                .sanctionDate(LocalDate.now())
                .sanctionedBy("Penil")
                .remarks("loan Sanction latter successfully")
                .build();
    }

        @Override
        public void eSignSanctionLatter(Long loanId) {

                Loans loan = loanRepository.findById(loanId)
                                .orElseThrow(() -> new ResourceNotFoundException("Loan not found " + loanId));

                if (loan.getStatus() != LoanStatus.SANCTIONED) {
                        throw new InvalidLoanStatusException("Loan must be evaluated before sanctioning");
                }

                LoanSanction homeLoanSanction = homeLoanSanctionRepository.findByLoans_LoanId(loanId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "HomeLoanSanction not found " + loanId));

                homeLoanSanction.setESigned(true);
                loan.setESign(true);
                loanRepository.save(loan);
                homeLoanSanctionRepository.save(homeLoanSanction);
        }

}
