package com.bms.loan.service.impl;

import com.bms.loan.Repository.LoanDocumentRepository;
import com.bms.loan.Repository.LoanRepository;
import com.bms.loan.dto.request.LoanDocumentRequest;
import com.bms.loan.dto.response.loan.LoanDocumentResponse;
import com.bms.loan.entity.LoanDocument;
import com.bms.loan.entity.loan.Loans;
import com.bms.loan.enums.DocumentType;
import com.bms.loan.enums.KycStatus;
import com.bms.loan.exception.ResourceNotFoundException;
import com.bms.loan.service.LoanDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanDocumentServiceImpl implements LoanDocumentService {

    @Autowired
    private LoanDocumentRepository loanDocumentRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanDocumentMapper mapper;

    public LoanDocumentResponse uploadDocument(MultipartFile file, LoanDocumentRequest request) {
        try {
            // the loan application
            Loans loanApplication = loanRepository.findByLoanId(request.getLoanApplicationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Loan Application not found with id: " + request.getLoanApplicationId()));

            LoanDocument document = LoanDocument.builder()
                    .loans(loanApplication)
                    .documentType(DocumentType.valueOf(request.getDocumentType()))
                    .documentName(file.getOriginalFilename())
                    .documentNumber(request.getDocumentNumber())
                    .documentData(file.getBytes())
                    .kycStatus(KycStatus.PENDING)
                    .remarks(request.getRemarks())
                    .build();

            return mapper.toResponse(loanDocumentRepository.save(document));
        } catch (IOException e) {
            throw new RuntimeException("Failed to process file: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<LoanDocumentResponse> getDocumentsByLoanApplication(Long loanApplicationId) {
        return loanDocumentRepository.findByLoans_LoanId(loanApplicationId)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public LoanDocumentResponse verifyDocument(Long documentId) {
        LoanDocument document = loanDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        document.setKycStatus(KycStatus.VERIFIED);
        document.setRemarks("Document verified");
        return mapper.toResponse(loanDocumentRepository.save(document));
    }

    @Override
    public LoanDocument getDocument(Long documentId) {
        return loanDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> downloadDocument(Long documentId) {
        LoanDocument document = loanDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));

        // Detect content type by file name (simple approach)
        String contentType = detectContentType(document.getDocumentName());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getDocumentName() + "\"")
                .body(document.getDocumentData());
    }

    private String detectContentType(String fileName) {
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".pdf")) return "application/pdf";
        return "application/octet-stream";
    }
}
