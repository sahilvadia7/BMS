package com.bms.gateway.dto.response;

import com.bms.gateway.enums.PaymentStatus;
import com.bms.gateway.model.Payment;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

	private String paymentId;
	private String transactionId;
	private BigDecimal amount;
	private PaymentStatus status;
	private String providerReference;

	public static PaymentResponse fromEntity(Payment p) {
		return PaymentResponse.builder()
				.paymentId(p.getId())
				.transactionId(p.getTransactionId())
				.amount(p.getAmount())
				.status(p.getStatus())
				.providerReference(p.getProviderReference())
				.build();
	}
}

