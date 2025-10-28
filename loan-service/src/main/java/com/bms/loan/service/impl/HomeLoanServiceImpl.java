package com.bms.loan.service.impl;

import com.bms.loan.Repository.HomeLoanRepository;
import com.bms.loan.Repository.HomeVerificationReportRepository;
import com.bms.loan.Repository.InterestRateRepository;
import com.bms.loan.Repository.LoanRepository;
import com.bms.loan.dto.request.home.HomeVerificationRequestDto;
import com.bms.loan.dto.response.home.HomeLoanDisbursementResponseDTO;
import com.bms.loan.dto.response.home.HomeLoanSanctionResponseDTO;
import com.bms.loan.dto.response.home.HomeVerificationResponse;
import com.bms.loan.dto.response.loan.LoanEvaluationResponse;
import com.bms.loan.entity.InterestRate;
import com.bms.loan.entity.home.HomeLoanDetails;
import com.bms.loan.entity.home.HomeVerificationReport;
import com.bms.loan.entity.loan.Loans;
import com.bms.loan.enums.LoanStatus;
import com.bms.loan.service.HomeLoanService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;


// Verification → Evaluation → Sanction → Disbursement

@Service
@RequiredArgsConstructor
public class HomeLoanServiceImpl implements HomeLoanService {


    private final LoanRepository loanRepository;
    private final HomeLoanRepository homeLoanRepository;
    private final InterestRateRepository interestRateRepository;
    private final HomeVerificationReportRepository homeVerificationReportRepository;



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
//    private BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, int months) {
//        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12 * 100), RoundingMode.HALF_UP);
//        BigDecimal numerator = principal.multiply(monthlyRate).multiply((BigDecimal.ONE.add(monthlyRate)).pow(months));
//        BigDecimal denominator = ((BigDecimal.ONE.add(monthlyRate)).pow(months)).subtract(BigDecimal.ONE);
//        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
//    }
//
//    // Helper for readable remarks
//    private String getEvaluationRemarks(boolean approved, boolean ltv, boolean credit, boolean income, boolean down) {
//        if (approved) return "Loan approved after evaluation.";
//        StringBuilder sb = new StringBuilder("Loan rejected due to: ");
//        if (!ltv) sb.append("High requested amount; ");
//        if (!credit) sb.append("Low credit score; ");
//        if (!income) sb.append("Insufficient income; ");
//        if (!down) sb.append("Low down payment; ");
//        return sb.toString();
//    }

    @Override
    public LoanEvaluationResponse evaluateLoan(Long loanId) {

//        Loans loan = loanRepository.findById(loanId)
//                .orElseThrow(() -> new EntityNotFoundException("Loan not found"));
//
//        HomeLoanDetails details = homeLoanRepository.findByLoans_LoanId(loanId)
//                .orElseThrow(() -> new EntityNotFoundException("Home loan details not found"));
//
//        InterestRate rate = interestRateRepository.findByLoanType(String.valueOf(loan.getLoanType()));
//
//        //  Property-based eligibility (LTV)
//        BigDecimal maxEligible = details.getPropertyValue().multiply(BigDecimal.valueOf(0.8)); // 80% LTV rule
//        boolean withinLtv = loan.getRequestedAmount().compareTo(maxEligible) <= 0;
//
//        //  Credit Score check
//        int creditScore = loan.getCreditScore(); // assume present in Loans entity
//        boolean creditOk = creditScore >= 700;
//
//        //  Income-to-EMI ratio (simplified)
//        BigDecimal monthlyIncome = loan.getCustomer().getMonthlyIncome(); // assuming join available
//        BigDecimal interestRate = rate.getBaseRate();
//        BigDecimal emi = calculateEmi(loan.getRequestedAmount(), interestRate, loan.getRequestedTenureMonths());
//        boolean incomeOk = monthlyIncome != null && emi.compareTo(monthlyIncome.multiply(BigDecimal.valueOf(0.4))) <= 0;
//
//        //  Down payment sanity
//        boolean downPaymentOk = details.getDownPayment().compareTo(details.getPropertyValue().multiply(BigDecimal.valueOf(0.1))) >= 0; // at least 10%
//
//        //  Overall decision
//        boolean approved = withinLtv && creditOk && incomeOk && downPaymentOk;
//
//        BigDecimal approvedAmount = approved ? loan.getRequestedAmount() : maxEligible;
//
//        //  Update Loan entity
//        loan.setApprovedAmount(approvedAmount);
//        loan.setInterestRate(interestRate);
//        loan.setStatus(approved ? LoanStatus.EVALUATED : LoanStatus.REJECTED);
//        loanRepository.save(loan);
//
//        //  Build response
//        return LoanEvaluationResponse.builder()
//                .loanId(loanId)
//                .loanType(String.valueOf(loan.getLoanType()))
//                .approved(approved)
//                .remarks(getEvaluationRemarks(approved, withinLtv, creditOk, incomeOk, downPaymentOk))
//                .status(loan.getStatus().name())
//                .build();






        Loans loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found"));

        HomeLoanDetails details = homeLoanRepository.findByLoans_LoanId(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Home loan details not found"));

        InterestRate rate = interestRateRepository.findByLoanType(String.valueOf(loan.getLoanType()));

        // Basic evaluation logic (you can expand later)
        BigDecimal eligibleAmount = details.getPropertyValue().multiply(BigDecimal.valueOf(0.8)); // 80% LTV
        boolean approved = loan.getRequestedAmount().compareTo(eligibleAmount) <= 0;

        LoanEvaluationResponse response = new LoanEvaluationResponse();
        response.setLoanId(loanId);
        response.setLoanType(String.valueOf(loan.getLoanType()));
        response.setApproved(approved);
        response.setRemarks(approved ?
                "Eligible for requested amount" :
                "Requested amount exceeds 80% of property value");
        response.setStatus(approved ? "EVALUATED" : "REJECTED");

        // Update loan fields
        loan.setApprovedAmount(approved ? loan.getRequestedAmount() : eligibleAmount);
        loan.setInterestRate(rate.getBaseRate());
        loan.setStatus(approved ? LoanStatus.EVALUATED : LoanStatus.REJECTED);
        loanRepository.save(loan);

        return response;
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
}
