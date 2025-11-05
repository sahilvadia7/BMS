package com.bms.loan.service.impl;

import com.bms.loan.enums.DocumentType;
import com.bms.loan.service.impl.ocr.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentValidationService {

    private final OcrService ocrService;

    public boolean validateDocumentType(MultipartFile file, String declaredType) {
        try {
            String extractedText = ocrService.extractTextFromPdf(file);
            System.out.println("Extracted Text from PDF: " + extractedText);
            DocumentType detected = detectDocumentType(extractedText);
            return detected.name().equalsIgnoreCase(declaredType);
        } catch (Exception e) {
            throw new RuntimeException("Document validation failed: " + e.getMessage());
        }
    }

    private DocumentType detectDocumentType(String text) {
        text = text.toUpperCase();

        if (text.contains("AADHAAR") || text.matches(".*\\d{4}\\s\\d{4}\\s\\d{4}.*")) {
            return DocumentType.AADHAAR;
        }
        if (text.contains("INCOME TAX") || text.matches(".*[A-Z]{5}[0-9]{4}[A-Z]{1}.*")) {
            return DocumentType.PAN;
        }
        return DocumentType.UNKNOWN;
    }


}
