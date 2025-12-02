package com.bms.loan.service.impl;

import com.bms.loan.Repository.*;
import com.bms.loan.Repository.education.EducationLoanRepository;
import com.bms.loan.Repository.home.HomeLoanRepository;
import com.bms.loan.dto.request.InterestRateRequest;
import com.bms.loan.dto.request.loan.LoanHistory.ActiveLoanDto;
import com.bms.loan.dto.request.loan.LoanHistory.ClosedLoanDto;
import com.bms.loan.dto.response.InterestRateResponse;
import com.bms.loan.dto.response.car.CarLoanInfo;
import com.bms.loan.dto.response.emi.EmiSummary;
import com.bms.loan.dto.response.home.HomeLoanInfo;
import com.bms.loan.dto.response.loan.LoanDetailsAdminDto;
import com.bms.loan.dto.response.loan.LoanDetailsResponse;
import com.bms.loan.dto.response.loan.LoanSanctionDto;
import com.bms.loan.entity.InterestRate;
import com.bms.loan.entity.car.CarLoanDetails;
import com.bms.loan.entity.education.EducationLoanDetails;
import com.bms.loan.entity.home.HomeLoanDetails;
import com.bms.loan.entity.home.LoanSanction;
import com.bms.loan.entity.loan.ActiveLoan;
import com.bms.loan.entity.loan.ClosedLoan;
import com.bms.loan.entity.loan.LoanEmiSchedule;
import com.bms.loan.entity.loan.Loans;
import com.bms.loan.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Mapper {

    private final LoanRepository loansRepository;
    private final CarLoanRepository carLoanRepo;
    private final HomeLoanRepository homeLoanRepo;
    private final EducationLoanRepository educationLoanRepo;
    private final LoanSanctionRepository loanSanctionRepo;
    private final LoanHistoryDetailsRepository loanHistoryDetailsRepo;
    private final ActiveLoanRepository activeLoanRepo;
    private final CloseLoansRepository closeLoansRepo;


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


    public List<LoanDetailsAdminDto> loanDetailsAdminDtoList(List<Loans> loansList){
        List<LoanDetailsAdminDto> list = new ArrayList<>();

        for (Loans loan : loansList) {
            LoanDetailsAdminDto dto = new LoanDetailsAdminDto();

            dto.setLoanId(loan.getLoanId());
            dto.setCifNumber(loan.getCifNumber());
            dto.setLoanType(loan.getLoanType());

            dto.setInterestRate(loan.getInterestRate());
            dto.setRequestedAmount(loan.getRequestedAmount());
            dto.setApprovedAmount(loan.getApprovedAmount());
            dto.setRequestedTenureMonths(loan.getRequestedTenureMonths());

            dto.setOutstandingAmount(loan.getOutstandingAmount());
            dto.setTotalAmountPaid(loan.getTotalAmountPaid());
            dto.setTotalInterestPaid(loan.getTotalInterestPaid());
            dto.setTotalLateFee(loan.getTotalLateFee());
            dto.setTotalPaidEmiCount(loan.getTotalPaidEmiCount());
            dto.setStatus(loan.getStatus());

            dto.setAppliedAt(loan.getAppliedAt());
            dto.setUpdatedAt(loan.getUpdatedAt());

            dto.setMonthlyIncome(loan.getMonthlyIncome());
            dto.setEmploymentType(loan.getEmploymentType());

            dto.setBankName(loan.getBankName());
            dto.setBankAccount(loan.getBankAccount());
            dto.setIfscCode(loan.getIfscCode());

            dto.setDisbursementDate(loan.getDisbursementDate());
            dto.setNextDueDate(loan.getNextDueDate());
            dto.setESign(loan.isESign());

            switch (loan.getLoanType()) {
                case CAR -> {
                    CarLoanDetails car = carLoanRepo.findByLoans_LoanId(loan.getLoanId())
                            .orElseThrow(() -> new ResourceNotFoundException("Car Loan not found with id: "+loan.getLoanId()));

                    CarLoanInfo carLoanInfo = CarLoanInfo.builder()
                            .carModel(car.getCarModel())
                            .manufacturer(car.getManufacturer())
                            .manufactureYear(car.getManufactureYear())
                            .carValue(car.getCarValue())
                            .registrationNumber(car.getRegistrationNumber())
                            .carAgeYears(car.getCarAgeYears())
                            .carConditionScore(car.getCarConditionScore())
                            .downPayment(car.getDownPayment())
                            .insuranceValid(car.isInsuranceValid())
                            .employmentStabilityYears(car.getEmploymentStabilityYears())
                            .build();
                    dto.setCarLoanDetails(carLoanInfo);


                }
                case HOME -> {
                    HomeLoanDetails home = homeLoanRepo.findByLoans_LoanId(loan.getLoanId())
                            .orElseThrow(() -> new ResourceNotFoundException("Home Loan not found with id: "+loan.getLoanId()));


                    HomeLoanInfo homeLoanInfo = HomeLoanInfo.builder()
                            .propertyAddress(home.getPropertyAddress())
                            .propertyValue(home.getPropertyValue())
                            .builderName(home.getBuilderName())
                            .downPayment(home.getDownPayment())
                            .propertyType(home.getPropertyType())
                            .loanToValueRatio(home.getLoanToValueRatio())
                            .ownershipType(home.getOwnershipType())
                            .registrationNumber(home.getRegistrationNumber())
                            .approvedByAuthority(home.isApprovedByAuthority())
                            .build();
                    dto.setHomeLoanDetails(homeLoanInfo);

                }
                case EDUCATION -> {
                    EducationLoanDetails education = educationLoanRepo.findByLoans_LoanId(loan.getLoanId())
                            .orElseThrow(() -> new ResourceNotFoundException("Education Loan not found with id: "+loan.getLoanId()));

                }
            }

            Optional<LoanSanction> sanction = loanSanctionRepo.findByLoans_LoanId(loan.getLoanId());
            if (sanction.isPresent()) {
                LoanSanctionDto loanSanctionDto = LoanSanctionDto.builder()
                        .sanctionedAmount(sanction.get().getSanctionedAmount())
                        .interestRate(sanction.get().getInterestRate())
                        .tenureMonths(sanction.get().getTenureMonths())
                        .emiAmount(sanction.get().getEmiAmount())
                        .sanctionDate(sanction.get().getSanctionDate())
                        .build();
            }


            if (loan.getLoanHistoryDetails() != null) {
                List<ActiveLoan> activeLoans =
                        activeLoanRepo.findByLoanHistoryDetailsId(loan.getLoanHistoryDetails().getId());

                dto.setActiveLoans(activeLoanMapper(activeLoans));

                List<ClosedLoan> closedLoans =
                        closeLoansRepo.findByLoanHistoryDetailsId(loan.getLoanHistoryDetails().getId());

                dto.setClosedLoans(closeLoanMapper(closedLoans));
            }
            list.add(dto);
        }
        return list;
    }

    public List<ActiveLoanDto> activeLoanMapper (List<ActiveLoan> activeLoans){
        return activeLoans.stream()
                .map(active -> ActiveLoanDto.builder()
                        .loanType(active.getLoanType())
                        .loanAmount(active.getLoanAmount())
                        .tenureMonths(active.getTenureMonths())
                        .remainingAmount(active.getRemainingAmount())
                        .emiAmount(active.getEmiAmount())
                        .startDate(active.getStartDate())
                        .endDate(active.getEndDate())
                        .bankOrLenderName(active.getBankOrLenderName())
                        .totalEmis(active.getTotalEmis())
                        .timelyPaidEmis(active.getTimelyPaidEmis())
                        .lateOrMissedEmis(active.getLateOrMissedEmis())
                        .build()
                )
                .collect(Collectors.toList());
    }

    public List<ClosedLoanDto> closeLoanMapper (List<ClosedLoan> closedLoans){
        return closedLoans.stream()
                .map(closed -> ClosedLoanDto.builder()
                        .loanType(closed.getLoanType())
                        .loanAmount(closed.getLoanAmount())
                        .startDate(closed.getStartDate())
                        .endDate(closed.getEndDate())
                        .bankOrLenderName(closed.getBankOrLenderName())
                        .closedOnTime(closed.isClosedOnTime())
                        .closureReason(closed.getClosureReason())
                        .build()
                )
                .collect(Collectors.toList());
    }
}
