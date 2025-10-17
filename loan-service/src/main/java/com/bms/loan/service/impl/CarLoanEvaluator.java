package com.bms.loan.service.impl;

import com.bms.loan.Repository.CarLoanRepository;
import com.bms.loan.Repository.LoanRepository;
import com.bms.loan.dto.response.LoanEvaluationResult;
import com.bms.loan.entity.CarLoanDetails;
import com.bms.loan.entity.Loans;
import com.bms.loan.enums.LoanStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CarLoanEvaluator {

    private final LoanRepository loansRepository;
    private final CarLoanRepository carLoanRepository;

    public CarLoanEvaluator(LoanRepository loansRepository, CarLoanRepository carLoanRepository) {
        this.loansRepository = loansRepository;
        this.carLoanRepository = carLoanRepository;
    }

    public LoanEvaluationResult evaluateCarLoan(Long loanId) {
        Loans loan = loansRepository.findById(Math.toIntExact(loanId))
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        // 1️⃣ Common checks
        if (!commonChecks(loan.getCustomerId() , loan)) {
            return new LoanEvaluationResult(false, loan.getRemarks(), LoanStatus.REJECTED);
        }



        // 2️⃣ Car-specific checks
        CarLoanDetails car = carLoanRepository.findByLoans_LoanId(loanId)
                .orElseThrow(() -> new RuntimeException("Car loan not found"));
        if (car == null) {
            loan.setStatus(LoanStatus.REJECTED);
            loan.setRemarks("Car loan details missing");
            loansRepository.save(loan);
            return new LoanEvaluationResult(false, loan.getRemarks(), LoanStatus.REJECTED);
        }

        // Car Age Check
        if (car.getCarAgeYears() > 8) {
            loan.setStatus(LoanStatus.REJECTED);
            loan.setRemarks("Car too old for loan (max 8 years)");
            loansRepository.save(loan);
            return new LoanEvaluationResult(false, loan.getRemarks(), LoanStatus.REJECTED);
        }

        // Condition check
        if (car.getCarConditionScore() < 5) {
            loan.setStatus(LoanStatus.REJECTED);
            loan.setRemarks("Car condition below acceptable level");
            loansRepository.save(loan);
            return new LoanEvaluationResult(false, loan.getRemarks(), LoanStatus.REJECTED);
        }

        // Down payment ratio check (min 10%)
        BigDecimal minDownPayment = car.getCarValue().multiply(BigDecimal.valueOf(0.1));
        if (car.getDownPayment().compareTo(minDownPayment) < 0) {
            loan.setStatus(LoanStatus.REJECTED);
            loan.setRemarks("Insufficient down payment (<10%)");
            loansRepository.save(loan);
            return new LoanEvaluationResult(false, loan.getRemarks(), LoanStatus.REJECTED);
        }

        // Loan-to-Value (LTV)
        BigDecimal ltv = loan.getRequestedAmount()
                .divide(car.getCarValue(), 2, RoundingMode.HALF_UP);
        if (ltv.compareTo(BigDecimal.valueOf(0.85)) > 0) {
            loan.setStatus(LoanStatus.REJECTED);
            loan.setRemarks("LTV exceeds 85%");
            loansRepository.save(loan);
            return new LoanEvaluationResult(false, loan.getRemarks(), LoanStatus.REJECTED);
        }

        // Employment Stability Check
        if (car.getEmploymentStabilityYears() < 1) {
            loan.setStatus(LoanStatus.REJECTED);
            loan.setRemarks("Employment too short (<1 year)");
            loansRepository.save(loan);
            return new LoanEvaluationResult(false, loan.getRemarks(), LoanStatus.REJECTED);
        }

        // Insurance validity
        if (!car.isInsuranceValid()) {
            loan.setStatus(LoanStatus.REJECTED);
            loan.setRemarks("Insurance not valid or expired");
            loansRepository.save(loan);
            return new LoanEvaluationResult(false, loan.getRemarks(), LoanStatus.REJECTED);
        }

        // Tenure check
        if (loan.getRequestedTenureMonths() > 60) {
            loan.setStatus(LoanStatus.REJECTED);
            loan.setRemarks("Tenure exceeds max allowed for car loan (60 months)");
            loansRepository.save(loan);
            return new LoanEvaluationResult(false, loan.getRemarks(), LoanStatus.REJECTED);
        }

        // 3️⃣ All checks passed → approve
        loan.setStatus(LoanStatus.APPROVED);
        loan.setRemarks("Car loan approved for evaluation");
        loansRepository.save(loan);
        return new LoanEvaluationResult(true, loan.getRemarks(), LoanStatus.APPLIED);
    }

    private boolean commonChecks(Long customerId, Loans loan) {

        int creditScore = 0;
        boolean blacklisted = false;
        BigDecimal monthlyIncome = null;
        BigDecimal totalExistingEmi = null;

        // If internal customerId exists, you can fetch internal data
        if (loan.getCustomerId() != null) {

            // here need to call account service api for getting this kind of data
            // TODO: fetch internal customer data from your Customer repository
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
