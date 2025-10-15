package com.bms.loan.dto.response;

import com.bms.loan.enums.LoanType;
import com.bms.loan.enums.LoanStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanRejectionResponseDto {
    private Long id;
    private Long customerId;
    private String accountNumber;
    private LoanType productCode;
    private BigDecimal principal;
    private Integer tenureMonths;
    private BigDecimal annualRate;
    private LoanStatus status;
    private String rejectedReason;
    private LocalDateTime appliedAt;
    private LocalDateTime rejectedAt;
    private String message;
}

