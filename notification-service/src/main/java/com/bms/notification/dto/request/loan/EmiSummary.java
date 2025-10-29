package com.bms.notification.dto.request.loan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmiSummary {

    private Long emiId;
    private int installmentNumber;
    private LocalDate dueDate;
    private BigDecimal emiAmount;
    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private LocalDate paymentDate;
    private BigDecimal paidAmount;
    private BigDecimal lateFee;
    private Integer daysLate;
    private String remarks;
}
