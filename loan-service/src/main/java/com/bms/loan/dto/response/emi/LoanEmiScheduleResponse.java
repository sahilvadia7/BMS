package com.bms.loan.dto.response.emi;

import com.bms.loan.enums.EmiStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanEmiScheduleResponse {
    private Long id;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal emiAmount;
    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private EmiStatus status;
    private LocalDate paymentDate;
    private BigDecimal lateFee;
}
