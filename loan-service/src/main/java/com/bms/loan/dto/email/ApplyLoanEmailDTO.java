package com.bms.loan.dto.email;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyLoanEmailDTO {
    private String email;
    private String customerName;
    private Long loanId;
    private String cifNumber;
}
