package com.bms.gateway.model;

import com.bms.gateway.enums.Currency;
import com.bms.gateway.enums.TransactionStatus;
import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "external_transactions",
		indexes = {@Index(name = "idx_external_ref", columnList = "externalReferenceId")})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String transactionId;

	@Column(nullable = false)
	private String sourceAccount;

	@Column(nullable = false)
	private String destinationAccount;

	private String destinationBankCode;

	@Column(nullable = false)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	private Currency currency;

	@Enumerated(EnumType.STRING)
	private TransactionStatus status;

	private String failureReason;

	@Column(unique = true)
	private String externalReferenceId;

	private String gatewayProvider;

	private LocalDateTime initiatedAt;
	private LocalDateTime completedAt;
}
