package com.bms.loan.service.impl.document;

import com.bms.loan.Repository.LoanDocumentRepository;
import com.bms.loan.Repository.LoanRepository;
import com.bms.loan.config.AadhaarVerificationService;
import com.bms.loan.dto.request.loan.LoanDocumentRequest;
import com.bms.loan.dto.response.loan.LoanDocumentResponse;
import com.bms.loan.entity.LoanDocument;
import com.bms.loan.entity.loan.Loans;
import com.bms.loan.enums.DocumentType;
import com.bms.loan.enums.KycStatus;
import com.bms.loan.exception.InvalidDocumentTypeException;
import com.bms.loan.exception.ResourceNotFoundException;
import com.bms.loan.service.LoanDocumentService;
import com.bms.loan.service.impl.DocumentValidationService;

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

    private final LoanDocumentRepository loanDocumentRepository;
    private final LoanRepository loanRepository;
    private final DocumentValidationService documentValidationService;
    private final AadhaarVerificationService aadhaarVerificationService;
    private final LoanDocumentMapper mapper;

    public LoanDocumentServiceImpl(LoanDocumentRepository loanDocumentRepository,
            LoanRepository loanRepository,
            DocumentValidationService documentValidationService,
            AadhaarVerificationService aadhaarVerificationService,
            LoanDocumentMapper mapper) {
        this.loanDocumentRepository = loanDocumentRepository;
        this.loanRepository = loanRepository;
        this.documentValidationService = documentValidationService;
        this.aadhaarVerificationService = aadhaarVerificationService;
        this.mapper = mapper;
    }

    public LoanDocumentResponse uploadDocument(MultipartFile file, LoanDocumentRequest request) {
        try {
            // the loan application
            Loans loanApplication = loanRepository.findByLoanId(request.getLoanApplicationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Loan Application not found with id: " + request.getLoanApplicationId()));

            boolean validType = documentValidationService.validateDocumentType(file, request.getDocumentType(),
                    request.getDocumentNumber());

            System.out.println("Document Validated: " + validType);
            if (!validType) {
                throw new InvalidDocumentTypeException("Uploaded document does not match the declared type: "
                        + request.getDocumentType());
            }

            // Validate document type before saving
            LoanDocument document = LoanDocument.builder()
                    .loans(loanApplication)
                    .documentType(DocumentType.valueOf(request.getDocumentType()))
                    .documentName(file.getOriginalFilename())
                    .documentNumber(request.getDocumentNumber())
                    .documentData(file.getBytes())
                    .kycStatus(KycStatus.PENDING)
                    .remarks(request.getRemarks())
                    .build();

            document = loanDocumentRepository.save(document);

            String docType = request.getDocumentType().toUpperCase();
            String docNumber = request.getDocumentNumber();

            if (docType.equals("AADHAAR") && docNumber != null) {
                boolean isValid = aadhaarVerificationService.verifyAadhaar(docNumber);
                document.setKycStatus(isValid ? KycStatus.VERIFIED : KycStatus.INVALID);
                document.setRemarks(isValid ? "Aadhaar verified successfully" : "Aadhaar verification failed");
            } else if (docType.equals("PAN") && docNumber != null) {
                boolean isValid = aadhaarVerificationService.verifyPan(docNumber);
                document.setKycStatus(isValid ? KycStatus.VERIFIED : KycStatus.INVALID);
                document.setRemarks(isValid ? "PAN verified successfully" : "PAN verification failed");
            }

            loanDocumentRepository.save(document);

            return mapper.toResponse(document);
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

    public LoanDocumentResponse rejectDocument(Long documentId) {
        LoanDocument document = loanDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        document.setKycStatus(KycStatus.REJECTED);
        document.setRemarks("Document rejected");
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
        if (fileName.endsWith(".png"))
            return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
            return "image/jpeg";
        if (fileName.endsWith(".pdf"))
            return "application/pdf";
        return "application/octet-stream";
    }
}
