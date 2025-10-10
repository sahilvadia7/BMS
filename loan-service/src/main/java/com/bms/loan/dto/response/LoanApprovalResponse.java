package com.bms.loan.dto.response;

import com.bms.loan.enums.LoanStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanApprovalResponse {
    private Long loanId;
    private LoanStatus status;
    private BigDecimal approvedAmount;
    private BigDecimal approvedRate;
    private Integer approvedTenure;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String message;
}
