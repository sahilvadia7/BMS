package com.bms.loan.dto.request.loan;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDocumentRequest {

    private Long loanApplicationId;
    private String documentType;
    private String documentName;
    private String documentNumber;
    private String remarks;
}
