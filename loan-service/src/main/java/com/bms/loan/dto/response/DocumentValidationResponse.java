package com.bms.loan.dto.response;

import com.bms.loan.enums.DocumentType;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentValidationResponse {
    private DocumentType detectedType;
    private boolean numberMatched;
    private String extractedNumber;
    private String message;
}
