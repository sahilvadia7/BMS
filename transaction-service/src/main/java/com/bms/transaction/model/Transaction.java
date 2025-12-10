package com.bms.transaction.model;

import com.bms.transaction.enums.Currency;
import com.bms.transaction.enums.TransactionStatus;
import com.bms.transaction.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
		name = "transactions",
		indexes = {
				@Index(name = "idx_txn_id", columnList = "transactionId"),
				@Index(name = "idx_idempotency", columnList = "idempotencyKey"),
				@Index(name = "idx_account", columnList = "accountNumber"),
				@Index(name = "idx_ext_ref", columnList = "externalReferenceId")
		}
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String transactionId;

	@Column(nullable = false)
	private String accountNumber;

	private String destinationAccountNumber;

	@Enumerated(EnumType.STRING)
	private TransactionType transactionType;

	@Column(nullable = false, precision = 14, scale = 2)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	private Currency currency;

	private BigDecimal fee;
	private boolean chargeable;

	/** MOBILE_APP / WEB / BRANCH / ATM / UPI / API */
	private String initiatedBy;

	private String idempotencyKey;

	/** Channel-level tracking (ATM slip number, UPI Ref No, Web Session ID) */
	private String channelReferenceId;

	private String branchCode;

	private boolean pinVerified;

	@Column(length = 200)
	private String requestHash;


	private String linkedTransactionId;

	@Enumerated(EnumType.STRING)
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

	@PrePersist
	public void prePersist() {
		if (transactionDate == null)
			transactionDate = LocalDateTime.now();
		if (initiatedAt == null)
			initiatedAt = LocalDateTime.now();
		if (transactionId == null)
			transactionId = "TXN-" + System.currentTimeMillis();
	}
}
