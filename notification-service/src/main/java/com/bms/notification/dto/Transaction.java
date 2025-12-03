package com.bms.notification.dto;

import com.bms.notification.Currency;
import com.bms.notification.TransactionStatus;
import com.bms.notification.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

	private Long id;

	private String transactionId;

	private String accountNumber;

	private String destinationAccountNumber;

	private TransactionType transactionType;

	private BigDecimal amount;

	private Currency currency;

	private BigDecimal fee;
	private boolean chargeable;

	private String initiatedBy;

	private String idempotencyKey;

	private String channelReferenceId;

	private String branchCode;

	private boolean pinVerified;

	private String requestHash;

	private String linkedTransactionId;

	private TransactionStatus status;

	private String description;

	private LocalDateTime transactionDate;

	private LocalDateTime completedAt;

	private String failureReason;

	private String destinationBankCode;

	private String gatewayProvider;

	private String externalReferenceId;

	private boolean gatewayRetryRequired;

	private LocalDateTime initiatedAt;

	private LocalDateTime CompensatedAt;

	private LocalDateTime gatewayAcknowledgedAt;

}
