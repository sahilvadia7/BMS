package com.bms.gateway.dto.response;

import com.bms.gateway.enums.RefundStatus;
import com.bms.gateway.model.Refund;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RefundResponse {

	private Long refundId;
	private Long paymentId;
	private Long amount;
	private String reason;
	private RefundStatus status;
	private LocalDateTime requestedAt;
	private LocalDateTime updatedAt;

	public static RefundResponse from(Refund refund) {
		return RefundResponse.builder()
				.refundId(refund.getId())
				.paymentId(refund.getPayment().getId())
				.amount(refund.getAmount())
				.reason(refund.getReason())
				.status(refund.getStatus())
				.requestedAt(refund.getRequestedAt())
				.updatedAt(refund.getUpdatedAt())
				.build();
	}
}

