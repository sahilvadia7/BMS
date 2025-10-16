package com.bms.loan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanEvaluationResponse {
    private Long loanId;
    private boolean approved;
    private String remarks;
    private String loanType;
    private String status;
}
