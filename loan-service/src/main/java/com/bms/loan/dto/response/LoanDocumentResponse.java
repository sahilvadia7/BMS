package com.bms.loan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDocumentResponse {

    private Long loanApplicationId;

    private String documentType;

    private String documentName;

    private String documentPath;

    private String kycStatus;

    private String remarks;

    private LocalDateTime uploadedAt;
}
