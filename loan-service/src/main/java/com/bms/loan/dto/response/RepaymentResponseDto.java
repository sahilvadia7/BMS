package com.bms.loan.dto.response;

import com.bms.loan.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RepaymentResponseDto {
    private Long loanId;
    private BigDecimal paidAmount;
    private BigDecimal outstandingBalance;
    private PaymentStatus emiStatus;
    private String paymentMode;
    private String txnRef;
    private LocalDateTime paymentDate;
    private String message;
}
