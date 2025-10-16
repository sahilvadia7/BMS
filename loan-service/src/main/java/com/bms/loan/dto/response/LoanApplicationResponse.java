package com.bms.loan.dto.response;

import com.bms.loan.enums.LoanStatus;
import com.bms.loan.enums.LoanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationResponse {
    private Long loanId;
    private String loanType;
    private String status;
    private String message;
}
