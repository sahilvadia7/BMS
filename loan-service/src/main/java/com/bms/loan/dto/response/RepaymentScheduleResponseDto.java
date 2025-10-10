package com.bms.loan.dto.response;

import com.bms.loan.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepaymentScheduleResponseDto {

    private Long id;
    private LocalDate dueDate;
    private BigDecimal principalDue;
    private BigDecimal interestDue;
    private BigDecimal emi;
    private BigDecimal paidAmount;
    private PaymentStatus status;
}
