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
            return new LoanEvaluationResult(false, loan.getRemarks());
        }


        // 2️⃣ Car-specific checks
        CarLoanDetails car = carLoanRepository.findByLoans_LoanId(loanId)
                .orElseThrow(() -> new RuntimeException("Car loan not found"));
        if (car == null) {
            loan.setStatus(LoanStatus.REJECTED);
            loan.setRemarks("Car loan details missing");
            loansRepository.save(loan);
            return new LoanEvaluationResult(false, loan.getRemarks());
        }

        BigDecimal ltv = loan.getRequestedAmount()
                .divide(car.getCarValue(), 2, RoundingMode.HALF_UP);
        if (ltv.compareTo(BigDecimal.valueOf(0.9)) > 0) {
            loan.setStatus(LoanStatus.REJECTED);
            loan.setRemarks("LTV too high: " + ltv);
            loansRepository.save(loan);
            return new LoanEvaluationResult(false, loan.getRemarks());
        }

        if (loan.getRequestedTenureMonths() > 60) {
            loan.setStatus(LoanStatus.REJECTED);
            loan.setRemarks("Tenure exceeds max allowed for car loan");
            loansRepository.save(loan);
            return new LoanEvaluationResult(false, loan.getRemarks());
        }

        // 3️⃣ All checks passed → approve
        loan.setStatus(LoanStatus.EVALUATED);
        loan.setRemarks("Car loan approved for evaluation");
        loansRepository.save(loan);
        return new LoanEvaluationResult(true, loan.getRemarks());
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
