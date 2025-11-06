package com.bms.creditscore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simplified response for loan eligibility checks
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditCheckResponse {
    private Long customerId;
    private boolean eligible;
    private int cibilScore;
    private String status;
    private String message;
    private double minimumScoreRequired;
}
