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
        text = text.toUpperCase()
                  .replaceAll("\\u00A0", " ")   // normalize non-breaking spaces
                  .replaceAll("\\s+", " ");     // normalize all whitespace

        if (text.contains("AADHAAR") ||
                text.contains("UNIQUE IDENTIFICATION AUTHORITY OF INDIA") ||
                text.matches(".*\\d{4}\\s\\d{4}\\s\\d{4}.*") ||
                text.contains("GOVERNMENT OF INDIA")) {
            return DocumentType.AADHAAR;
        }
        if (text.contains("INCOME TAX") ||
                text.matches("(?s).*[A-Z]{5}[0-9]{4}[A-Z]{1}.*") ||
                text.matches("GOVT. OF INDIA") ||
                text.contains("INCOMETAX")) {
            return DocumentType.PAN;
        }
        return DocumentType.UNKNOWN;
    }


}
