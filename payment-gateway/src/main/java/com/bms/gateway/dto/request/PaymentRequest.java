package com.bms.gateway.dto.request;

import com.bms.gateway.enums.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

@Data
public class PaymentRequest {

	@NotBlank(message = "Transaction ID is required")
	private String transactionId;

	@NotNull(message = "Amount is required")
	@DecimalMin(value = "0.01", message = "Amount must be greater than zero")
	private BigDecimal amount;

	@NotNull(message = "Payment method is required")
	private PaymentMethod method;

	@NotBlank(message = "Idempotency Key is required")
	@Size(max = 50, message = "Idempotency Key cannot exceed 50 characters")
	private String idempotencyKey;

	@Size(max = 500, message = "Metadata JSON cannot exceed 500 characters")
	private String metadataJson; // Optional field
}
