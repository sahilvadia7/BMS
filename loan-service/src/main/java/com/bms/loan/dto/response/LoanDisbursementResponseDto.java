package com.bms.loan.dto.response;

import com.bms.loan.enums.LoanProductCode;
import com.bms.loan.enums.LoanStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LoanDisbursementResponseDto {
    private Long id;
    private Long customerId;
    private String accountNumber;
    private LoanProductCode productCode;
    private BigDecimal principal;
    private Integer tenureMonths;
    private BigDecimal annualRate;
    private LoanStatus status;
    private BigDecimal outstandingBalance;
    private LocalDateTime appliedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime disbursedAt;
    private String approvedBy;
    private List<RepaymentScheduleResponseDto> repaymentSchedules;
    private String message;
}
