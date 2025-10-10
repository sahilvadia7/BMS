package com.bms.loan.dto.response;

import com.bms.loan.enums.LoanProductCode;
import com.bms.loan.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationResponse {
    private Long id;
    private Long customerId;
    private String accountNumber;
    private LoanProductCode productCode;
    private BigDecimal principal;
    private Integer tenureMonths;
    private BigDecimal annualRate;
    private LoanStatus status;
    private BigDecimal outstandingBalance;
    private Integer creditScore;
    private LocalDateTime appliedAt;
}
