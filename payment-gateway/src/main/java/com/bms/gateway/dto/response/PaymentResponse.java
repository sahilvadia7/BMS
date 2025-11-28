package com.bms.gateway.dto.response;

import com.bms.gateway.enums.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {

	private String transactionId;
	private String externalReferenceId;
	private TransactionStatus status;
	private BigDecimal amount;
	private String destinationBankCode;
	private LocalDateTime initiatedAt;
	private LocalDateTime completedAt;
	private String failureReason;
}
