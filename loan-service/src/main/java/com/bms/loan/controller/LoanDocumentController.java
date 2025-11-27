package com.bms.loan.controller;

import com.bms.loan.dto.request.loan.LoanDocumentRequest;
import com.bms.loan.dto.response.loan.LoanDocumentResponse;
import com.bms.loan.service.LoanDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loan-documents")
@Tag(name = "Loan Document Management", description = "Endpoints for uploading and verifying loan documents")
public class LoanDocumentController {

    private final LoanDocumentService service;

    public LoanDocumentController(LoanDocumentService service) {
        this.service = service;
    }

    // Upload new document
    @Operation(summary = "Upload loan document", description = "Access: Customer")
    @PostMapping(value = "/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LoanDocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestPart LoanDocumentRequest request) {
        return ResponseEntity.ok(service.uploadDocument(file, request));
    }

    // Get all documents by loan application id
    @Operation(summary = "Get documents by loan application ID", description = "Access: Internal (Loan Officer), Admin")
    @GetMapping("/loan/{loanApplicationId}")
    public ResponseEntity<List<LoanDocumentResponse>> getDocumentsByLoanApplication(
            @PathVariable Long loanApplicationId) {
        return ResponseEntity.ok(service.getDocumentsByLoanApplication(loanApplicationId));
    }

    // Verify or reject document (KYC)
    @Operation(summary = "Verify or reject document", description = "Access: Internal (Loan Officer)")
    @PutMapping("/{documentId}/verify")
    public ResponseEntity<LoanDocumentResponse> verifyDocument(
            @PathVariable Long documentId) {
        return ResponseEntity.ok(service.verifyDocument(documentId));
    }

    @Operation(summary = "Download document", description = "Access: Internal (Loan Officer), Admin")
    @GetMapping("/{documentId}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long documentId) {
        return service.downloadDocument(documentId);
    }

    private String getContentType(String fileName) {
        if (fileName.endsWith(".pdf"))
            return "application/pdf";
        else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
            return "image/jpeg";
        else if (fileName.endsWith(".png"))
            return "image/png";
        else
            return "application/octet-stream"; // default for unknown files
    }

}
