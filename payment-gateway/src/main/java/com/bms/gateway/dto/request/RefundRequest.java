package com.bms.gateway.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefundRequest {

	@NotNull(message = "Payment ID is required")
	private String paymentId;

	@NotNull(message = "Refund amount is required")
	@Min(value = 1, message = "Refund amount must be greater than 0")
	private Long amount;

	private String reason;
}