package com.bms.creditscore.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for credit score calculation
 * All fields except customerId are optional and will use defaults if not provided
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditScoreRequest {

    private Long customerId; // Optional: if null, manual calculation mode

    @Min(value = 0, message = "Payment history must be between 0 and 100")
    @Max(value = 100, message = "Payment history must be between 0 and 100")
    private Double paymentHistory; // % of on-time payments (0-100)

    @Min(value = 0, message = "Credit utilization must be between 0 and 100")
    @Max(value = 100, message = "Credit utilization must be between 0 and 100")
    private Double creditUtilization; // % of credit used (0-100)

    @Min(value = 0, message = "Credit mix must be between 0 and 100")
    @Max(value = 100, message = "Credit mix must be between 0 and 100")
    private Double creditMix; // Variety of credit types (0-100)

    @Min(value = 0, message = "Credit age cannot be negative")
    @Max(value = 50, message = "Credit age cannot exceed 50 years")
    private Double creditAge; // Years of credit history

    @Min(value = 0, message = "Recent inquiries cannot be negative")
    @Max(value = 20, message = "Recent inquiries seems unrealistic")
    private Integer recentInquiries; // Number of recent hard inquiries
}
