package com.bms.creditscore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@AllArgsConstructor
public class CreditScoreResponse {
    private Long customerId;
    private int cibilScore;
    private double repoRateUsed;
    private String status;
    private String source; // EXISTING | NEW | MANUAL | FALLBACK
    private Instant calculatedAt;
    private Map<String, Object> breakdown; // optional detailed numbers
}

