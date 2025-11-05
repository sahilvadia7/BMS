package com.bms.loan.service.impl.document;

import com.bms.loan.dto.request.LoanDocumentRequest;
import com.bms.loan.dto.response.loan.LoanDocumentResponse;
import com.bms.loan.entity.LoanDocument;
import com.bms.loan.entity.loan.Loans;
import com.bms.loan.enums.DocumentType;
import com.bms.loan.enums.KycStatus;
import org.springframework.stereotype.Component;

@Component
public class LoanDocumentMapper {

    public LoanDocument toEntity(LoanDocumentRequest request, Loans loans) {
        return LoanDocument.builder()
                .loans(loans)
                .documentType(DocumentType.valueOf(request.getDocumentType()))
                .documentName(request.getDocumentName())
                .kycStatus(KycStatus.PENDING) // default when uploading
                .remarks(request.getRemarks())
                .build();
    }

    public LoanDocumentResponse toResponse(LoanDocument entity) {
        return LoanDocumentResponse.builder()
                .loanApplicationId(entity.getLoans().getLoanId())
                .documentType(String.valueOf(entity.getDocumentType()))
                .documentName(entity.getDocumentName())
                .kycStatus(String.valueOf(entity.getKycStatus()))
                .remarks(entity.getRemarks())
                .uploadedAt(entity.getUploadedAt())
                .build();
    }
}
