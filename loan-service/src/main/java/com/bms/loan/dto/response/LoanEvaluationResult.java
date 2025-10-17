package com.bms.loan.dto.response;

import com.bms.loan.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanEvaluationResult {
    private boolean approved;
    private String remarks;
    private LoanStatus status;

//    public LoanEvaluationResult(boolean b, String remarks ,LoanStatus status) {
//    }
}
