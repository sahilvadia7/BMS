package com.bms.loan.service.impl;

import com.bms.loan.dto.request.InterestRateRequest;
import com.bms.loan.dto.response.InterestRateResponse;
import com.bms.loan.dto.response.car.CarLoanInfo;
import com.bms.loan.dto.response.emi.EmiSummary;
import com.bms.loan.dto.response.home.HomeLoanInfo;
import com.bms.loan.dto.response.loan.LoanDetailsResponse;
import com.bms.loan.entity.InterestRate;
import com.bms.loan.entity.car.CarLoanDetails;
import com.bms.loan.entity.home.HomeLoanDetails;
import com.bms.loan.entity.loan.LoanEmiSchedule;
import com.bms.loan.entity.loan.Loans;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Mapper {

    public InterestRate toEntity(InterestRateRequest request) {
        return InterestRate.builder()
                .loanType(request.getLoanType().toUpperCase())
                .baseRate(request.getBaseRate())
                .maxLTV(request.getMaxLTV())
                .minTenure(request.getMinTenure())
                .maxTenure(request.getMaxTenure())
                .build();
    }

    public InterestRateResponse toResponse(InterestRate entity) {
        return InterestRateResponse.builder()
                .id(entity.getId())
                .loanType(entity.getLoanType())
                .baseRate(entity.getBaseRate())
                .maxLTV(entity.getMaxLTV())
                .minTenure(entity.getMinTenure())
                .maxTenure(entity.getMaxTenure())
                .build();
    }

    // Convert Loan entity → LoanDetailsResponse DTO
    public LoanDetailsResponse toLoanDetailsResponse(Loans loan,
                                                     HomeLoanDetails homeDetails,
                                                     CarLoanDetails carDetails) {

        LoanDetailsResponse.LoanDetailsResponseBuilder builder = LoanDetailsResponse.builder()
                .loanId(loan.getLoanId())
                .cifNumber(loan.getCifNumber())
                .loanType(loan.getLoanType())
                .status(loan.getStatus())
                .bankName(loan.getBankName())
                .bankAccount(loan.getBankAccount())
                .ifscCode(loan.getIfscCode())
                .requestedAmount(loan.getRequestedAmount())
                .approvedAmount(loan.getApprovedAmount())
                .interestRate(loan.getInterestRate())
                .tenureMonths(loan.getRequestedTenureMonths())
                .outstandingAmount(loan.getOutstandingAmount())
                .totalAmountPaid(loan.getTotalAmountPaid())
                .totalInterestPaid(loan.getTotalInterestPaid())
                .totalLateFee(loan.getTotalLateFee())
                .totalPaidEmiCount(loan.getTotalPaidEmiCount())
                .disbursementDate(loan.getDisbursementDate())
                .nextDueDate(loan.getNextDueDate())
                .remarks(loan.getRemarks())
                .appliedAt(loan.getAppliedAt())
                .updatedAt(loan.getUpdatedAt());

        if (homeDetails != null) {
            builder.homeLoanDetails(
                    HomeLoanInfo.builder()
                            .propertyAddress(homeDetails.getPropertyAddress())
                            .propertyValue(homeDetails.getPropertyValue())
                            .builderName(homeDetails.getBuilderName())
                            .downPayment(homeDetails.getDownPayment())
                            .propertyType(homeDetails.getPropertyType())
                            .loanToValueRatio(homeDetails.getLoanToValueRatio())
                            .ownershipType(homeDetails.getOwnershipType())
                            .registrationNumber(homeDetails.getRegistrationNumber())
                            .approvedByAuthority(homeDetails.isApprovedByAuthority())
                            .build()
            );
        }

        if (carDetails != null) {
            builder.carLoanDetails(
                    CarLoanInfo.builder()
                            .carModel(carDetails.getCarModel())
                            .manufacturer(carDetails.getManufacturer())
                            .manufactureYear(carDetails.getManufactureYear())
                            .carValue(carDetails.getCarValue())
                            .registrationNumber(carDetails.getRegistrationNumber())
                            .carAgeYears(carDetails.getCarAgeYears())
                            .carConditionScore(carDetails.getCarConditionScore())
                            .downPayment(carDetails.getDownPayment())
                            .insuranceValid(carDetails.isInsuranceValid())
                            .employmentStabilityYears(carDetails.getEmploymentStabilityYears())
                            .build()
            );
        }

        return builder.build();
    }

    //Convert EMI entity → EMI DTO
    public EmiSummary toEmiSummary(LoanEmiSchedule emi) {
        return EmiSummary.builder()
                .emiId(emi.getId())
                .installmentNumber(emi.getInstallmentNumber())
                .dueDate(emi.getDueDate())
                .emiAmount(emi.getEmiAmount())
                .principalComponent(emi.getPrincipalComponent())
                .interestComponent(emi.getInterestComponent())
                .status(emi.getStatus())
                .paymentDate(emi.getPaymentDate())
                .paidAmount(emi.getPaidAmount())
                .lateFee(emi.getLateFee())
                .daysLate(emi.getDaysLate())
                .remarks(emi.getRemarks())
                .build();
    }
}
