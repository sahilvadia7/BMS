package com.bms.transaction.dto.response;

import com.bms.transaction.enums.TransactionStatus;
import com.bms.transaction.model.Transaction;
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

	public static PaymentResponse failed(String transactionId,
										 BigDecimal amount,
										 String failureReason) {

		return PaymentResponse.builder()
				.transactionId(transactionId)
				.externalReferenceId(null)
				.status(TransactionStatus.FAILED)
				.amount(amount)
				.destinationBankCode(null)
				.initiatedAt(LocalDateTime.now())
				.completedAt(LocalDateTime.now())
				.failureReason(failureReason)
				.build();
	}

	public static PaymentResponse failed(Transaction txn) {
		return failed(
				txn.getTransactionId(),
				txn.getAmount(),
				txn.getFailureReason()
		);
	}
}
