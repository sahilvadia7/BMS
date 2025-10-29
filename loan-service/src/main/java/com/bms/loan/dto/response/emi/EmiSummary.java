package com.bms.loan.dto.response.emi;

import com.bms.loan.enums.EmiStatus;
import lombok.*;

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
    private EmiStatus status;
    private LocalDate paymentDate;
    private BigDecimal paidAmount;
    private BigDecimal lateFee;
    private Integer daysLate;
    private String remarks;
}
