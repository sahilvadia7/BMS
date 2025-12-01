package com.bms.loan.service.impl;

import com.bms.loan.enums.DocumentType;
import com.bms.loan.service.impl.ocr.OcrService;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentValidationService {

    private final OcrService ocrService;

    public DocumentValidationService(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    public boolean validateDocumentType(MultipartFile file, String declaredType, String documentNumber) {
        try {

            String extractedText = ocrService.extractTextFromPdf(file);
            System.out.println("Extracted Text from PDF: " + extractedText);
            DocumentType detected = detectDocumentType(extractedText, documentNumber);
            return detected.name().equalsIgnoreCase(declaredType);
        } catch (Exception e) {
            throw new RuntimeException("Document validation failed: " + e.getMessage());
        }
    }

    private DocumentType detectDocumentType(String text, String documentNumber) {
        text = text.toUpperCase()
                .replaceAll("\\u00A0", " ") // normalize non-breaking spaces
                .replaceAll("\\s+", " "); // normalize all whitespace

        if (text.contains("AADHAAR") ||
                text.contains("UNIQUE IDENTIFICATION AUTHORITY OF INDIA") ||
                text.matches(".*\\d{4}\\s\\d{4}\\s\\d{4}.*") ||
                text.contains("GOVERNMENT OF INDIA")) {

            // Aadhaar number regex pattern (#### #### ####)
            Pattern aadhaarPattern = Pattern.compile("\\b\\d{4}\\s\\d{4}\\s\\d{4}\\b");
            Matcher matcher = aadhaarPattern.matcher(text);

            if (matcher.find()) {
                String aadhaarNumber = matcher.group();
                if ((aadhaarNumber.replaceAll("\\s+", "")).equalsIgnoreCase(documentNumber)) {
                    System.out.println("AadhaarNumber match with Extracted  Aadhaar Number: " + documentNumber);
                }
                return DocumentType.AADHAAR;
            }

            // fallback — maybe the OCR missed spaces (like 12-digit continuous number)
            Pattern aadhaarContinuous = Pattern.compile("\\b\\d{12}\\b");
            Matcher matcher2 = aadhaarContinuous.matcher(text);

            if (matcher2.find()) {
                String aadhaarNumber = matcher2.group();
                System.out.println("Extracted Aadhaar Number (no spaces): " + aadhaarNumber);
                System.out.println("AadhaarNumber match with Extracted  Aadhaar Number: " + documentNumber);
                return DocumentType.AADHAAR;
            }

            return DocumentType.AADHAAR;
        }
        if (text.contains("INCOME TAX") ||
                text.matches("(?s).*[A-Z]{5}[0-9]{4}[A-Z]{1}.*") ||
                text.matches("GOVT. OF INDIA") ||
                text.contains("INCOMETAX")) {


            Pattern panPattern = Pattern.compile("\\b[A-Z]{5}[0-9]{4}[A-Z]{1}\\b");
            Matcher matcher = panPattern.matcher(text);

            if (matcher.find()) {
                String extractedPan = matcher.group();
                System.out.println("Extracted PAN Number: " + extractedPan);

                // If documentNumber provided and matches
                if (documentNumber != null &&
                        extractedPan.equalsIgnoreCase(documentNumber)) {
                    System.out.println("PAN Number MATCHES with provided documentNumber: " + documentNumber);
                }

                return DocumentType.PAN;
            }

            Pattern panLoosePattern = Pattern.compile("\\b[A-Z0-9]{10}\\b");
            Matcher matcher2 = panLoosePattern.matcher(text);

            if (matcher2.find()) {
                String extractedPanLoose = matcher2.group();
                System.out.println("Extracted Possible PAN (loose check): " + extractedPanLoose);
                return DocumentType.PAN;
            }
            return DocumentType.PAN;
        }
        return DocumentType.UNKNOWN;
    }

}
