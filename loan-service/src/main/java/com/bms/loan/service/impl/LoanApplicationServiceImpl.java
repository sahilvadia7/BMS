package com.bms.loan.service.impl;

import com.bms.loan.Repository.*;
import com.bms.loan.dto.request.*;
import com.bms.loan.dto.response.*;
import com.bms.loan.entity.*;
import com.bms.loan.enums.EmiStatus;
import com.bms.loan.enums.LoanStatus;
import com.bms.loan.feign.CustomerClient;
import com.bms.loan.service.LoanApplicationService;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.util.Objects;


@Service
@RequiredArgsConstructor
@Primary
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private final LoanRepository loansRepository;
    private final CarLoanRepository carLoanRepo;
    private final HomeLoanRepository homeLoanRepo;
    private final EducationLoanRepository educationLoanRepo;
    private final InterestRateRepository interestRateRepository;
    private final CarLoanEvaluator carLoanEvaluator;
    private final LoanEmiScheduleRepository loanEmiScheduleRepository;
    private final CustomerClient customerClient;

    @Override
    public LoanApplicationResponse applyLoan(LoanApplicationRequest request) {

        InterestRate interestRate = interestRateRepository.findByLoanType(String.valueOf(request.getLoanType()));

        if (interestRate == null) {
            throw new RuntimeException("Interest rate not found with id: ");
        }
        if (request.getRequestedTenureMonths() > interestRate.getMaxTenure()){
            throw new RuntimeException("Tenure months cannot be greater than interest rate maximum");
        }

        CustomerResponseDTO customer;
        try {
            customer = customerClient.getCustomerById(request.getCustomerId());
        } catch (FeignException e) {
            if (Objects.equals(e.getMessage(), "Customer not found")) { // Customer not found
                // Call register customer
                customer = customerClient.registerCustomer(request.getCustomerDetails());
            } else {
                // Other HTTP errors (500, etc.)
                throw e;
            }
        }

        // Create main loan record
        Loans loan = Loans.builder()
                .customerId(request.getCustomerId())
                .cifNumber(customer.getCifNumber())
                .loanType(request.getLoanType())
                .interestRate(interestRate.getBaseRate())
                .requestedAmount(request.getRequestedAmount())
                .requestedTenureMonths(request.getRequestedTenureMonths())
                .totalAmountPaid(BigDecimal.valueOf(0))
                .bankName(request.getBankName())
                .bankAccount(request.getBankAccount())
                .ifscCode(request.getIfscCode())
                .status(LoanStatus.APPLIED)
                .build();

        Loans savedLoan = loansRepository.save(loan);

        // Save specific details based on loan type

        switch (request.getLoanType()) {
            case CAR -> {
                CarLoanDetailsDto cd = request.getCarDetails();

                int currentYear = Year.now().getValue();

                carLoanRepo.save(CarLoanDetails.builder()
                        .loans(savedLoan)
                        .carModel(cd.getCarModel())
                        .manufacturer(cd.getManufacturer())
                        .manufactureYear(cd.getManufactureYear())
                        .carValue(cd.getCarValue())
                        .registrationNumber(cd.getRegistrationNumber())
                        .carAgeYears(currentYear- cd.getManufactureYear())
                        .build());
            }
            case HOME -> {
                HomeLoanDetailsDto hd = request.getHomeDetails();
                homeLoanRepo.save(HomeLoanDetails.builder()
                        .loans(savedLoan)
                        .propertyAddress(hd.getPropertyAddress())
                        .propertyValue(hd.getPropertyValue())
                        .builderName(hd.getBuilderName())
                        .downPayment(hd.getDownPayment())
                        .build());
            }
            case EDUCATION -> {
                EducationLoanDetailsDto ed = request.getEducationDetails();
                educationLoanRepo.save(EducationLoanDetails.builder()
                        .loans(savedLoan)
                        .courseName(ed.getCourseName())
                        .university(ed.getUniversity())
                        .courseDurationMonths(ed.getCourseDurationMonths())
                        .tuitionFees(ed.getTuitionFees())
                        .coApplicantName(ed.getCoApplicantName())
                        .build());
            }
            default -> throw new IllegalArgumentException("Unsupported loan type");
        }

        //Build and return response
        return LoanApplicationResponse.builder()
                .loanId(savedLoan.getLoanId())
                .loanType(String.valueOf(savedLoan.getLoanType()))
                .status(String.valueOf(savedLoan.getStatus()))
                .message("Loan application submitted successfully.")
                .build();
    }

    @Override
    public CarLoanEvaluationByBankResponse updateEvaluationData(Long loanId, CarLoanEvaluationRequestDto request) {
        CarLoanDetails carLoan = carLoanRepo.findByLoans_LoanId(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Car loan not found for loanId: " + loanId));

        if (request.getDownPayment() != null)
            carLoan.setDownPayment(request.getDownPayment());

        carLoan.setInsuranceValid(request.isInsuranceValid());
        carLoan.setCarConditionScore(request.getCarConditionScore());
        carLoan.setEmploymentStabilityYears(request.getEmploymentStabilityYears());

        CarLoanDetails saved = carLoanRepo.save(carLoan);

        // Make sure loans object is loaded
        Long loanIdValue = saved.getLoans().getLoanId();

        return CarLoanEvaluationByBankResponse.builder()
                .loanId(loanIdValue)
                .carModel(saved.getCarModel())
                .manufacturer(saved.getManufacturer())
                .manufactureYear(saved.getManufactureYear())
                .carValue(saved.getCarValue())
                .registrationNumber(saved.getRegistrationNumber())
                .carAgeYears(saved.getCarAgeYears())
                .downPayment(saved.getDownPayment())
                .insuranceValid(saved.isInsuranceValid())
                .carConditionScore(saved.getCarConditionScore())
                .employmentStabilityYears(saved.getEmploymentStabilityYears())
                .build();
    }

    @Override
    public LoanEvaluationResponse evaluateLoan(Long loanId) {
        Loans loan = loansRepository.findById(Math.toIntExact(loanId))
                .orElseThrow(() -> new EntityNotFoundException("Loan not found with id: " + loanId));

        LoanEvaluationResponse loanEvaluationResponse = new LoanEvaluationResponse();
        loanEvaluationResponse.setLoanId(loanId);

        switch (loan.getLoanType()) {
            case CAR -> {
                LoanEvaluationResult result = carLoanEvaluator.evaluateCarLoan(loanId);
                loanEvaluationResponse.setLoanType(String.valueOf(loan.getLoanType()));
                loanEvaluationResponse.setApproved(result.isApproved());
                loanEvaluationResponse.setRemarks(result.getRemarks());
                loanEvaluationResponse.setStatus(String.valueOf(result.getStatus()));
            }
            case HOME, EDUCATION -> {
                // Future: integrate HomeLoanEvaluator / EducationLoanEvaluator
            }
            default -> throw new IllegalArgumentException("Unsupported loan type: " + loan.getLoanType());
        }

        // Return unified response
        return loanEvaluationResponse;
    }

    public LoanDisbursementResponse disburseLoan(Long loanId) {
        Loans loan = loansRepository.findById(Math.toIntExact(loanId))
                .orElseThrow(() -> new EntityNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException("Loan is not approved for disbursement");
        }

        // Update status & disbursement date
        loan.setStatus(LoanStatus.DISBURSED);
        loan.setDisbursementDate(LocalDate.now());
        loansRepository.save(loan);

        // Calculate EMI
        BigDecimal emi = calculateEmi(
                loan.getApprovedAmount(),
                loan.getInterestRate(),
                loan.getRequestedTenureMonths()
        );

        BigDecimal principalComponent = loan.getApprovedAmount()
                .divide(BigDecimal.valueOf(loan.getRequestedTenureMonths()), 2, RoundingMode.HALF_UP);

        LocalDate dueDate = LocalDate.now().plusMonths(1);
        for (int i = 1; i <= loan.getRequestedTenureMonths(); i++) {
            LoanEmiSchedule schedule = LoanEmiSchedule.builder()
                    .loan(loan)
                    .installmentNumber(i)
                    .dueDate(dueDate)
                    .emiAmount(emi)
                    .principalComponent(principalComponent)
                    .interestComponent(emi.subtract(principalComponent))
                    .status(EmiStatus.UNPAID)
                    .build();
            loanEmiScheduleRepository.save(schedule);
            dueDate = dueDate.plusMonths(1);
        }

        // Build response
        return LoanDisbursementResponse.builder()
                .loanId(loan.getLoanId())
                .status(String.valueOf(loan.getStatus()))
                .emi(emi)
                .message("Loan disbursed successfully and EMI schedule created")
                .build();

    // Optional: send email notification to customer
    }


    public BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRatePercent, int months) {
        double monthlyRate = annualRatePercent.doubleValue() / 12 / 100;
        double emi = principal.doubleValue() * monthlyRate * Math.pow(1 + monthlyRate, months)
                / (Math.pow(1 + monthlyRate, months) - 1);
        return BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);
    }

}
