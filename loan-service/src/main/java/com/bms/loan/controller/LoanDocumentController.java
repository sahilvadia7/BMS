package com.bms.loan.controller;

import com.bms.loan.dto.request.LoanDocumentRequest;
import com.bms.loan.dto.response.LoanDocumentResponse;
import com.bms.loan.entity.LoanDocument;
import com.bms.loan.enums.KycStatus;
import com.bms.loan.service.LoanDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/loan-documents")
public class LoanDocumentController {

    @Autowired
    private LoanDocumentService service;

    // Upload new document
    @PostMapping("/upload")
    public ResponseEntity<LoanDocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestBody LoanDocumentRequest request)
    {
        return ResponseEntity.ok(service.uploadDocument(file , request));
    }

    // Get all documents by loan application id
    @GetMapping("/loan/{loanApplicationId}")
    public ResponseEntity<List<LoanDocumentResponse>> getDocumentsByLoanApplication(@PathVariable Long loanApplicationId) {
        return ResponseEntity.ok(service.getDocumentsByLoanApplication(loanApplicationId));
    }

    // Verify or reject document (KYC)
    @PutMapping("/{documentId}/verify")
    public ResponseEntity<LoanDocumentResponse> verifyDocument(
            @PathVariable Long documentId,
            @RequestParam KycStatus status,
            @RequestParam(required = false) String remarks) {
        return ResponseEntity.ok(service.verifyDocument(documentId, status, remarks));
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long documentId) {
        LoanDocument doc = service.getDocument(documentId);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + doc.getDocumentName())
                .body(doc.getDocumentData());
    }

    private String getContentType(String fileName) {
        if (fileName.endsWith(".pdf")) return "application/pdf";
        else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        else if (fileName.endsWith(".png")) return "image/png";
        else return "application/octet-stream"; // default for unknown files
    }

}
