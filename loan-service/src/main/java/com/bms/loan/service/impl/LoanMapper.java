package com.bms.loan.service.impl;

import com.bms.loan.dto.request.LoanApplicationDto;
import com.bms.loan.dto.response.LoanApplicationResponse;
import com.bms.loan.dto.response.LoanDisbursementResponseDto;
import com.bms.loan.dto.response.RepaymentScheduleResponseDto;
import com.bms.loan.entity.LoanApplication;
import com.bms.loan.entity.RepaymentSchedule;
import com.bms.loan.enums.LoanStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Component
public class LoanMapper {

    public LoanApplication toEntity(LoanApplicationDto dto, Long userId) {
        return LoanApplication.builder()
                .customerId(userId)
                .accountNumber(dto.getAccountNumber())
                .productCode(dto.getProductCode())
                .principal(dto.getPrincipal())
                .tenureMonths(dto.getTenureMonths())
                .annualRate(dto.getAnnualRate())
                .creditScore(dto.getCreditScore())
                .status(LoanStatus.APPLIED)
                .outstandingBalance(dto.getPrincipal())
                .appliedAt(LocalDateTime.now())
                .build();
    }

    public LoanApplicationResponse toResponse(LoanApplication entity) {
        return LoanApplicationResponse.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .accountNumber(entity.getAccountNumber())
                .productCode(entity.getProductCode())
                .principal(entity.getPrincipal())
                .tenureMonths(entity.getTenureMonths())
                .annualRate(entity.getAnnualRate())
                .status(entity.getStatus())
                .outstandingBalance(entity.getOutstandingBalance())
                .creditScore(entity.getCreditScore())
                .appliedAt(entity.getAppliedAt())
                .build();
    }

    public LoanDisbursementResponseDto toDisbursementResponse(LoanApplication loan) {
        if (loan == null) return null;

        return LoanDisbursementResponseDto.builder()
                .id(loan.getId())
                .customerId(loan.getCustomerId())
                .productCode(loan.getProductCode())
                .principal(loan.getPrincipal())
                .tenureMonths(loan.getTenureMonths())
                .annualRate(loan.getAnnualRate())
                .status(loan.getStatus())
                .outstandingBalance(loan.getOutstandingBalance())
                .appliedAt(loan.getAppliedAt())
                .approvedAt(loan.getApprovedAt())
                .disbursedAt(loan.getDisbursedAt())
                .approvedBy(loan.getApprovedBy())
                .repaymentSchedules(
                        loan.getRepaymentSchedules().stream()
                                .map(this::toRepaymentScheduleResponse)
                                .collect(Collectors.toList())
                )
                .message("Loan disbursed successfully")
                .build();
    }

    private RepaymentScheduleResponseDto toRepaymentScheduleResponse(RepaymentSchedule schedule) {
        return RepaymentScheduleResponseDto.builder()
                .id(schedule.getId())
                .dueDate(schedule.getDueDate())
                .emi(schedule.getEmi())
                .principalDue(schedule.getPrincipalDue())
                .interestDue(schedule.getInterestDue())
                .paidAmount(schedule.getPaidAmount())
                .status(schedule.getStatus())
                .build();
    }


    public static RepaymentScheduleResponseDto toRepaymentScheduleResponseDto(RepaymentSchedule schedule) {
        return RepaymentScheduleResponseDto.builder()
                .id(schedule.getId())
                .dueDate(schedule.getDueDate())
                .emi(schedule.getEmi())
                .principalDue(schedule.getPrincipalDue())
                .interestDue(schedule.getInterestDue())
                .paidAmount(schedule.getPaidAmount())
                .status(schedule.getStatus())
                .build();
    }
}
