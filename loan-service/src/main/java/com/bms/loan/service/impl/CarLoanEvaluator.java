package com.bms.loan.service.impl;

import com.bms.loan.Repository.car.CarLoanRepository;
import com.bms.loan.Repository.LoanRepository;
import com.bms.loan.Repository.car.CarVerificationReportRepository;
import com.bms.loan.dto.response.loan.LoanEvaluationResult;
import com.bms.loan.entity.car.CarLoanDetails;
import com.bms.loan.entity.car.CarVerificationReport;
import com.bms.loan.entity.loan.Loans;
import com.bms.loan.enums.LoanStatus;
import com.bms.loan.enums.RiskBand;
import com.bms.loan.exception.InvalidLoanStatusException;
import com.bms.loan.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CarLoanEvaluator {

    private final LoanRepository loansRepository;
    private final CarLoanRepository carLoanRepository;
    private final CarVerificationReportRepository carVerificationReportRepository;

    public CarLoanEvaluator(LoanRepository loansRepository, CarLoanRepository carLoanRepository, CarVerificationReportRepository carVerificationReportRepository) {
        this.loansRepository = loansRepository;
        this.carLoanRepository = carLoanRepository;
        this.carVerificationReportRepository = carVerificationReportRepository;
    }

    public LoanEvaluationResult evaluateCarLoan(Long loanId) {
        Loans loan = loansRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));


        if (loan.getStatus() != LoanStatus.VERIFIED) {
            throw new InvalidLoanStatusException("Loan must be Verified before Evaluating the Car Loan");
        }
        // 1: Common borrower checks
        if (!commonChecks(loan.getCustomerId() , loan)) {
            return new LoanEvaluationResult(false, loan.getRemarks(), LoanStatus.REJECTED);
        }


        // Car-specific checks
        CarLoanDetails car = carLoanRepository.findByLoans_LoanId(loanId)
                .orElseThrow(() -> new RuntimeException("Car loan details not found"));
        CarVerificationReport carVerificationReport = carVerificationReportRepository.findByLoans_LoanId(loanId)
                .orElseThrow(() -> new RuntimeException("Car verification report not found"));

        // 2: HARD REJECTIONS
        if (!carVerificationReport.isInsuranceValid()
                || car.getCarAgeYears() > 8
                || carVerificationReport.getCarConditionScore() < 5
                || carVerificationReport.getEmploymentStabilityYears() < 1
                || !carVerificationReport.isEmploymentVerified()
                || !carVerificationReport.isCarDocumentsVerified()
                || !carVerificationReport.isPhysicalCarInspectionDone()) {

            loan.setStatus(LoanStatus.REJECTED);
            loan.setRemarks("Car loan rejected due to verification failure");
            loansRepository.save(loan);
            return new LoanEvaluationResult(false, loan.getRemarks(), LoanStatus.REJECTED);
        }

        // 3: RISK SCORING
        int score = 0;

        //  Vehicle Risk (35)
        if (car.getCarAgeYears() <= 3) score += 15;
        else if (car.getCarAgeYears() <= 5) score += 10;

        score += Math.min(carVerificationReport.getCarConditionScore(), 10); // max 10

        // Borrower Risk (30)
        int creditScore = loan.getExternalCreditScore();
        if (creditScore >= 750) score += 15;
        else if (creditScore >= 700) score += 10;


        if (carVerificationReport.getEmploymentStabilityYears() >= 3) {
            score += 10;
        } else {
            score += 5; // guaranteed >= 1 here
        }



        // Financial Risk (25)
        BigDecimal ltv = loan.getRequestedAmount()
                .divide(car.getCarValue(), 2, RoundingMode.HALF_UP);

        if (ltv.compareTo(BigDecimal.valueOf(0.7)) <= 0) score += 15;
        else if (ltv.compareTo(BigDecimal.valueOf(0.85)) <= 0) score += 10;

        if (loan.getRequestedTenureMonths() <= 48) score += 10;
        else if (loan.getRequestedTenureMonths() <= 60) score += 5;

        //  Stability Risk (10)
        if (carVerificationReport.getNeighbourhoodStabilityScore() >= 7) score += 10;
        else if (carVerificationReport.getNeighbourhoodStabilityScore() >= 5) score += 5;

        // 4: DECISION
        RiskBand band;
        BigDecimal approvedAmount = loan.getRequestedAmount();

        if (score >= 75) {
            band = RiskBand.LOW;
        } else if (score >= 60) {
            band = RiskBand.MEDIUM;
        } else if (score >= 50) {
            band = RiskBand.HIGH;
            approvedAmount = approvedAmount.multiply(BigDecimal.valueOf(0.8)); // haircut
        } else {
            band = RiskBand.VERY_HIGH;
        }

        if (band == RiskBand.VERY_HIGH) {
            loan.setStatus(LoanStatus.REJECTED);
            loan.setRemarks("Rejected due to high risk score: " + score);
        } else {
            loan.setStatus(LoanStatus.EVALUATED);
            loan.setApprovedAmount(approvedAmount);
            loan.setRemarks("Approved under " + band + " risk (" + score + ")");
        }

        loansRepository.save(loan);

        return new LoanEvaluationResult(
                band != RiskBand.VERY_HIGH,
                loan.getRemarks(),
                loan.getStatus()
        );

    }

    private boolean commonChecks(Long customerId, Loans loan) {

        int creditScore = 0;
        boolean blacklisted = false;
        BigDecimal monthlyIncome = null;
        BigDecimal totalExistingEmi = null;

        // If internal customerId exists, you can fetch internal data
        if (loan.getCustomerId() != null) {

            // here need to call account service api for getting this kind of data
            creditScore = 750; // example, replace with actual
            blacklisted = false;
            monthlyIncome = BigDecimal.valueOf(50000);
            totalExistingEmi = BigDecimal.ZERO;
        } else {
            // External customer — use fields from Loans entity
            creditScore = loan.getExternalCreditScore() != null ? loan.getExternalCreditScore() : 700;
            blacklisted = loan.isExternalBlacklisted();
            monthlyIncome = loan.getExternalMonthlyIncome() != null ? loan.getExternalMonthlyIncome() : BigDecimal.valueOf(50000);
            totalExistingEmi = loan.getExternalTotalExistingEmi() != null ? loan.getExternalTotalExistingEmi() : BigDecimal.ZERO;
        }

        // Credit score check
        if (creditScore < 700) {
            loan.setStatus(LoanStatus.REJECTED);
            loan.setRemarks("Low credit score");
            loansRepository.save(loan);
            return false;
        }

        // Blacklist check
        if (blacklisted) {
            loan.setStatus(LoanStatus.REJECTED);
            loan.setRemarks("Customer blacklisted");
            loansRepository.save(loan);
            return false;
        }

        // EMI burden check
        BigDecimal maxAllowedEmi = monthlyIncome.multiply(BigDecimal.valueOf(0.4));
        if (totalExistingEmi.compareTo(maxAllowedEmi) > 0) {
            loan.setStatus(LoanStatus.REJECTED);
            loan.setRemarks("High EMI burden");
            loansRepository.save(loan);
            return false;
        }

        return true;
    }
}
