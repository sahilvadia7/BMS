package com.bms.loan.service.impl;

import com.bms.loan.dto.response.DocumentValidationResponse;
import com.bms.loan.enums.DocumentType;
import com.bms.loan.exception.InvalidDocumentTypeException;
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

    public DocumentValidationResponse  validateDocumentType(MultipartFile file, String declaredType, String documentNumber) {
        try {

            String extractedText = ocrService.extractTextFromPdf(file);
            System.out.println("Extracted Text from PDF: " + extractedText);
            DocumentValidationResponse  detected = detectDocumentType(extractedText, documentNumber);

            if (!detected.getDetectedType().name().equalsIgnoreCase(declaredType)) {
                detected.setMessage("Declared type does not match detected type.");
                return DocumentValidationResponse.builder()
                        .detectedType(DocumentType.UNKNOWN)
                        .numberMatched(detected.isNumberMatched())
                        .message("Uploaded document does not match the declared type")
                        .build();
            }

            return detected;

        }catch (Exception e) {
            return DocumentValidationResponse.builder()
                    .detectedType(DocumentType.UNKNOWN)
                    .numberMatched(false)
                    .message("Error processing document: " + e.getMessage())
                    .build();
        }
    }

    private DocumentValidationResponse detectDocumentType(String text, String documentNumber) {
        text = text.toUpperCase()
                .replaceAll("\\u00A0", " ") // normalize non-breaking spaces
                .replaceAll("\\s+", " "); // normalize all whitespace

        // AADHAAR DETECTION

        if (text.contains("AADHAAR") ||
                text.contains("UNIQUE IDENTIFICATION AUTHORITY OF INDIA") ||
                text.matches(".*\\d{4}\\s\\d{4}\\s\\d{4}.*") ||
                text.contains("GOVERNMENT OF INDIA")) {

            // Aadhaar number regex pattern (#### #### ####)
            Pattern aadhaarPattern = Pattern.compile("\\b\\d{4}\\s\\d{4}\\s\\d{4}\\b");
            Matcher matcher = aadhaarPattern.matcher(text);

            if (matcher.find()) {
                String extracted = matcher.group().replaceAll("\\s+", "");
                boolean match = extracted.equalsIgnoreCase(documentNumber);

                return DocumentValidationResponse.builder()
                        .detectedType(DocumentType.AADHAAR)
                        .extractedNumber(extracted)
                        .numberMatched(match)
                        .message(match ? "Aadhaar number valid." : "Aadhaar number does not match.")
                        .build();
            }

            // fallback — maybe the OCR missed spaces (like 12-digit continuous number)
            Pattern aadhaarContinuous = Pattern.compile("\\b\\d{12}\\b");
            Matcher matcher2 = aadhaarContinuous.matcher(text);

            if (matcher2.find()) {
                String extracted = matcher2.group();
                boolean match = extracted.equalsIgnoreCase(documentNumber);

                return DocumentValidationResponse.builder()
                        .detectedType(DocumentType.AADHAAR)
                        .extractedNumber(extracted)
                        .numberMatched(match)
                        .message(match ? "Aadhaar number valid." : "Aadhaar number does not match.")
                        .build();
            }
            return DocumentValidationResponse.builder()
                    .detectedType(DocumentType.UNKNOWN)
                    .extractedNumber(null)
                    .numberMatched(false)
                    .message("Could not extract Aadhaar number from document.")
                    .build();
        }


        // PAN DETECTION

        if (text.contains("INCOME TAX") ||
                text.matches("(?s).*[A-Z]{5}[0-9]{4}[A-Z]{1}.*") ||
                text.contains("GOVT. OF INDIA") ||
                text.contains("INCOMETAX") ||
                text.contains("PERMANENT ACCOUNT")) {


            Pattern panPattern = Pattern.compile("\\b[A-Z]{5}[0-9]{4}[A-Z]{1}\\b");
            Matcher matcher = panPattern.matcher(text);

            if (matcher.find()) {
                String extracted = matcher.group();
                boolean match = extracted.equalsIgnoreCase(documentNumber);

                return DocumentValidationResponse.builder()
                        .detectedType(DocumentType.PAN)
                        .extractedNumber(extracted)
                        .numberMatched(match)
                        .message(match ? "PAN number valid." : "PAN number does not match.")
                        .build();
            }

            Pattern panLoosePattern = Pattern.compile("\\b[A-Z0-9]{10}\\b");
            Matcher matcher2 = panLoosePattern.matcher(text);

            if (matcher2.find()) {
                String extracted = matcher2.group();
                boolean match = extracted.equalsIgnoreCase(documentNumber);

                return DocumentValidationResponse.builder()
                        .detectedType(DocumentType.PAN)
                        .extractedNumber(extracted)
                        .numberMatched(match)
                        .message(match ? "PAN number valid." : "PAN number does not match.")
                        .build();
            }

            return DocumentValidationResponse.builder()
                    .detectedType(DocumentType.UNKNOWN)
                    .extractedNumber(null)
                    .numberMatched(false)
                    .message("Could not extract PAN number from document.")
                    .build();
        }
        return DocumentValidationResponse.builder()
                .detectedType(DocumentType.UNKNOWN)
                .extractedNumber(null)
                .numberMatched(false)
                .message("Unknown document type.")
                .build();
    }

}
