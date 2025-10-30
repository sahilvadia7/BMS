package com.bms.loan.service.impl;

import com.bms.loan.Repository.home.HomeLoanRepository;
import com.bms.loan.Repository.home.HomeLoanSanctionRepository;
import com.bms.loan.Repository.home.HomeVerificationReportRepository;
import com.bms.loan.Repository.InterestRateRepository;
import com.bms.loan.Repository.LoanRepository;
import com.bms.loan.dto.email.SanctionEmailDTO;
import com.bms.loan.dto.request.home.HomeVerificationRequestDto;
import com.bms.loan.dto.request.home.LoanSanctionRequest;
import com.bms.loan.dto.response.CustomerDetailsResponseDTO;
import com.bms.loan.dto.response.home.HomeLoanDisbursementResponseDTO;
import com.bms.loan.dto.response.home.HomeLoanSanctionResponseDTO;
import com.bms.loan.dto.response.home.HomeVerificationResponse;
import com.bms.loan.dto.response.loan.LoanEvaluationResponse;
import com.bms.loan.entity.InterestRate;
import com.bms.loan.entity.home.HomeLoanDetails;
import com.bms.loan.entity.home.HomeLoanSanction;
import com.bms.loan.entity.home.HomeVerificationReport;
import com.bms.loan.entity.loan.Loans;
import com.bms.loan.enums.LoanStatus;
import com.bms.loan.exception.ResourceNotFoundException;
import com.bms.loan.feign.CustomerClient;
import com.bms.loan.feign.NotificationClient;
import com.bms.loan.service.HomeLoanService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;


// Verification → Evaluation → Sanction → Disbursement

@Service
@RequiredArgsConstructor
public class HomeLoanServiceImpl implements HomeLoanService {


    private final LoanRepository loanRepository;
    private final HomeLoanRepository homeLoanRepository;
    private final InterestRateRepository interestRateRepository;
    private final HomeVerificationReportRepository homeVerificationReportRepository;
    private final HomeLoanSanctionRepository homeLoanSanctionRepository;
    private final NotificationClient notificationClient;
    private final CustomerClient customerClient;


    @Override
    public HomeVerificationResponse verifyProperty(HomeVerificationRequestDto request) {
        // Validate Loan
        Loans loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan not found for ID: " + request.getLoanId()));

        // Save or update verification report
        HomeVerificationReport report = homeVerificationReportRepository
                .findByLoans_LoanId(request.getLoanId())
                .orElse(HomeVerificationReport.builder().loans(loan).build());

        report.setAddressVerified(request.isAddressVerified());
        report.setOwnershipVerified(request.isOwnershipVerified());
        report.setPropertyConditionOk(request.isPropertyConditionOk());
        report.setEvaluatedValue(request.getEvaluatedValue());
        report.setOfficerName(request.getOfficerName());
        report.setOfficerRemarks(request.getOfficerRemarks());
        report.setVisitDate(request.getVisitDate());

        homeVerificationReportRepository.save(report);

        // Update HomeLoanDetails (propertyValue or approvedByAuthority)
        HomeLoanDetails details = homeLoanRepository.findByLoans_LoanId(request.getLoanId())
                .orElseThrow(() -> new RuntimeException("HomeLoanDetails not found for Loan ID: " + request.getLoanId()));

        details.setPropertyValue(request.getEvaluatedValue());
        details.setApprovedByAuthority(true); // Mark as verified
        homeLoanRepository.save(details);

        // Optionally, mark loan as VERIFIED
        loan.setStatus(LoanStatus.EVALUATED);
        loanRepository.save(loan);

        // Return response
        return HomeVerificationResponse.builder()
                .loanId(loan.getLoanId())
                .verifiedSuccessfully(true)
                .evaluatedValue(report.getEvaluatedValue())
                .officerName(report.getOfficerName())
                .remarks(report.getOfficerRemarks())
                .verificationDate(report.getVisitDate())
                .status(loan.getStatus().name())
                .message("Property verification completed successfully.")
                .build();

    }

    // Simple EMI calculator
    private BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, int months) {
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12 * 100), RoundingMode.HALF_UP);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply((BigDecimal.ONE.add(monthlyRate)).pow(months));
        BigDecimal denominator = ((BigDecimal.ONE.add(monthlyRate)).pow(months)).subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    @Override
    public LoanEvaluationResponse evaluateLoan(Long loanId) {
        Loans loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found"));

        HomeLoanDetails details = homeLoanRepository.findByLoans_LoanId(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Home loan details not found"));


        InterestRate rate = interestRateRepository.findByLoanType(String.valueOf(loan.getLoanType()))
                .orElseThrow(() -> new ResourceNotFoundException("Interest Rate not found with Type: " + loan.getLoanType()));


        // Extract data safely
        BigDecimal propertyValue = details.getPropertyValue() != null ? details.getPropertyValue() : BigDecimal.ZERO;
        BigDecimal downPayment = details.getDownPayment() != null ? details.getDownPayment() : BigDecimal.ZERO;
        BigDecimal requestedAmount = loan.getRequestedAmount();
        BigDecimal income = loan.getMonthlyIncome() != null ? loan.getMonthlyIncome() : BigDecimal.ZERO;;
        int tenureMonths = loan.getRequestedTenureMonths() != null ? loan.getRequestedTenureMonths() : 240;
        int creditScore = loan.getExternalCreditScore();
        String employmentType = loan.getEmploymentType() != null ? loan.getEmploymentType().name() : "SALARIED";

        // Calculate ratios
        BigDecimal ltv = propertyValue.compareTo(BigDecimal.ZERO) > 0
                ? requestedAmount.divide(propertyValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.valueOf(100);

        BigDecimal downPaymentPercent = propertyValue.compareTo(BigDecimal.ZERO) > 0
                ? downPayment.divide(propertyValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        BigDecimal monthlyEmi = calculateEmi(requestedAmount, rate.getBaseRate(), tenureMonths);
        BigDecimal incomeToEmiRatio = income.compareTo(BigDecimal.ZERO) > 0
                ? monthlyEmi.divide(income, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.valueOf(100);

        // Assign points per rule
        int creditScorePoints;
        if (creditScore >= 750) creditScorePoints = 25;
        else if (creditScore >= 700) creditScorePoints = 20;
        else if (creditScore >= 650) creditScorePoints = 10;
        else creditScorePoints = 0;

        int ltvPoints;
        if (ltv.compareTo(BigDecimal.valueOf(80)) <= 0) ltvPoints = 25;
        else if (ltv.compareTo(BigDecimal.valueOf(90)) <= 0) ltvPoints = 15;
        else ltvPoints = 5;

        int incomePoints;
        if (incomeToEmiRatio.compareTo(BigDecimal.valueOf(40)) <= 0) incomePoints = 25;
        else if (incomeToEmiRatio.compareTo(BigDecimal.valueOf(50)) <= 0) incomePoints = 15;
        else incomePoints = 5;

        int downPaymentPoints;
        if (downPaymentPercent.compareTo(BigDecimal.valueOf(20)) >= 0) downPaymentPoints = 10;
        else if (downPaymentPercent.compareTo(BigDecimal.valueOf(10)) >= 0) downPaymentPoints = 5;
        else downPaymentPoints = 0;

        int employmentPoints = switch (employmentType.toUpperCase()) {
            case "SALARIED", "GOVERNMENT", "SELF_EMPLOYED" -> 10;
            default -> 5;
        };

        int tenurePoints = tenureMonths <= 240 ? 5 : 3;

        // Calculate total score
        int totalScore = creditScorePoints + ltvPoints + incomePoints + downPaymentPoints + employmentPoints + tenurePoints;

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

        //  Update loan record
        loan.setApprovedAmount(approved ? requestedAmount : requestedAmount.multiply(BigDecimal.valueOf(0.8)));
        loan.setInterestRate(finalInterestRate);
        loan.setStatus(approved ? LoanStatus.APPROVED : LoanStatus.REJECTED);
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

    @Override
    public HomeLoanSanctionResponseDTO sanctionLoan(Long loanId, String sanctionedBy) {
        Loans loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.EVALUATED) {
            throw new IllegalStateException("Loan must be evaluated before sanctioning");
        }

        loan.setStatus(LoanStatus.APPROVED);
        loanRepository.save(loan);

        return HomeLoanSanctionResponseDTO.builder()
                .loanId(loanId)
                .sanctionedAmount(loan.getApprovedAmount())
                .interestRate(loan.getInterestRate())
                .tenureMonths(loan.getRequestedTenureMonths())
                .sanctionDate(LocalDate.now())
                .sanctionedBy(sanctionedBy)
                .remarks("Loan sanctioned successfully")
                .build();
    }

    @Override
    public HomeLoanDisbursementResponseDTO disburseLoan(Long loanId) {
        Loans loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException("Loan not approved for disbursement");
        }

        loan.setStatus(LoanStatus.DISBURSED);
        loan.setDisbursementDate(LocalDate.now());
        loan.setOutstandingAmount(loan.getApprovedAmount());
        loanRepository.save(loan);

        return HomeLoanDisbursementResponseDTO.builder()
                .loanId(loanId)
                .disbursedAmount(loan.getApprovedAmount())
                .disbursementDate(LocalDate.now())
                .paymentMode("NEFT")
                .transactionRefNo("TXN" + loanId + System.currentTimeMillis())
                .remarks("Home loan amount disbursed successfully")
                .build();
    }




    @Transactional
    public HomeLoanSanctionResponseDTO sanctionHomeLoan(Long loanId, LoanSanctionRequest request) {
        Loans loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found "+loanId));

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException("Loan must be approved before sanctioning");
        }

        CustomerDetailsResponseDTO customer = customerClient.getByCif(loan.getCifNumber());

        BigDecimal principal = request.getSanctionedAmount();
        BigDecimal rate = request.getInterestRate();
        Integer tenure = request.getTenureMonths();

        BigDecimal emi = calculateEmi(principal, rate, tenure);

        // Save sanction
        HomeLoanSanction sanction = HomeLoanSanction.builder()
                .loans(loan)
                .sanctionedAmount(principal)
                .interestRate(rate)
                .tenureMonths(tenure)
                .sanctionDate(LocalDate.now())
                .eSigned(false)
                .build();

        homeLoanSanctionRepository.save(sanction);
        loanRepository.save(loan);

        // sanction email to customer
        SanctionEmailDTO email = SanctionEmailDTO.builder()
                .toEmail(customer.getEmail())
                .customerName(customer.getFirstName()+" "+customer.getLastName())
                .loanType(loan.getLoanType().name())
                .sanctionedAmount(principal)
                .interestRate(rate)
                .tenureMonths(tenure)
                .emiAmount(emi)
                .sanctionDate(LocalDate.now())
                .build();

        notificationClient.sendSanctionEmail(email);

        return HomeLoanSanctionResponseDTO.builder()
                .loanId(loan.getLoanId())
                .sanctionedAmount(principal)
                .interestRate(rate)
                .tenureMonths(tenure)
                .emiAmount(emi)
                .sanctionDate(LocalDate.now())
                .sanctionedBy("Penil")
                .remarks("Home loan Sanction latter successfully")
                .build();
    }

    @Override
    public void eSignSanctionLatter(Long loanId) {
        HomeLoanSanction homeLoanSanction = homeLoanSanctionRepository.findByLoans_LoanId(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("HomeLoanSanction not found "+loanId));

        homeLoanSanction.setESigned(true);

        homeLoanSanctionRepository.save(homeLoanSanction);
    }

}
