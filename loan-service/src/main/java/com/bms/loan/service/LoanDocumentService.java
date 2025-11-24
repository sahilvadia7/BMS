package com.bms.loan.service;

import com.bms.loan.dto.request.loan.LoanDocumentRequest;
import com.bms.loan.dto.response.loan.LoanDocumentResponse;
import com.bms.loan.entity.LoanDocument;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LoanDocumentService {

    LoanDocumentResponse uploadDocument(MultipartFile file, LoanDocumentRequest request);

    List<LoanDocumentResponse> getDocumentsByLoanApplication(Long loanApplicationId);

    LoanDocumentResponse verifyDocument(Long documentId);

    LoanDocument getDocument(Long documentId);

    ResponseEntity<byte[]> downloadDocument(Long documentId);
}
