package com.bms.loan.service.impl;

import com.bms.loan.Repository.*;
import com.bms.loan.dto.request.CarLoanDetailsDto;
import com.bms.loan.dto.request.EducationLoanDetailsDto;
import com.bms.loan.dto.request.HomeLoanDetailsDto;
import com.bms.loan.dto.request.LoanApplicationRequest;
import com.bms.loan.dto.response.LoanApplicationResponse;
import com.bms.loan.entity.*;
import com.bms.loan.enums.LoanStatus;
import com.bms.loan.service.LoanApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private final LoanRepository loansRepository;
    private final CarLoanRepository carLoanRepo;
    private final HomeLoanRepository homeLoanRepo;
    private final EducationLoanRepository educationLoanRepo;
    private final InterestRateRepository interestRateRepository;

    @Override
    public LoanApplicationResponse applyLoan(LoanApplicationRequest request) {

        InterestRate interestRate = interestRateRepository.findByLoanType(String.valueOf(request.getLoanType()));

        if (interestRate == null) {
            throw new RuntimeException("Interest rate not found with id: ");
        }
        if (request.getRequestedTenureMonths() > interestRate.getMaxTenure()){
            throw new RuntimeException("Tenure months cannot be greater than interest rate maximum");
        }


        // Create main loan record
        Loans loan = Loans.builder()
                .customerId(request.getCustomerId())
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
                carLoanRepo.save(CarLoanDetails.builder()
                        .loans(savedLoan)
                        .carModel(cd.getCarModel())
                        .manufacturer(cd.getManufacturer())
                        .manufactureYear(cd.getManufactureYear())
                        .carValue(cd.getCarValue())
                        .registrationNumber(cd.getRegistrationNumber())
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
}
