package com.bms.loan.dto.response.loan;


import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanPrepaymentResponse {
    private Long loanId;
    private BigDecimal newPrincipal;
    private BigDecimal newEmi;
    private Integer newTenureMonths;
    private String message;

}
