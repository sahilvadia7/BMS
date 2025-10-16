package com.bms.loan.service.impl;

import com.bms.loan.dto.response.RepaymentScheduleResponseDto;
import com.bms.loan.entity.RepaymentSchedule;
import org.springframework.stereotype.Component;

@Component
public class LoanMapper {

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
