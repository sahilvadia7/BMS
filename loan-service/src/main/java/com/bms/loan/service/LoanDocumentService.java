package com.bms.loan.service;

import com.bms.loan.dto.request.LoanDocumentRequest;
import com.bms.loan.dto.response.loan.LoanDocumentResponse;
import com.bms.loan.entity.LoanDocument;
import com.bms.loan.enums.KycStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LoanDocumentService {

    LoanDocumentResponse uploadDocument(MultipartFile file, LoanDocumentRequest request);

    List<LoanDocumentResponse> getDocumentsByLoanApplication(Long loanApplicationId);

    LoanDocumentResponse verifyDocument(Long documentId, KycStatus status, String remarks);

    LoanDocument getDocument(Long documentId);
}
