package com.bms.gateway.model;

import com.bms.gateway.enums.PaymentMethod;
import com.bms.gateway.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payments", indexes = {
		@Index(name = "idx_payment_txn_id", columnList = "transactionId"),
		@Index(name = "idx_payment_idem_key", columnList = "idempotencyKey", unique = true)
})
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false)
	private String transactionId;

	@Column(nullable = false)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentMethod method;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	private String providerPaymentId;
	private String providerReference;

	@Column(unique = true)
	private String idempotencyKey;

	@Column(columnDefinition = "TEXT")
	private String metadataJson;

	@Column(columnDefinition = "TEXT")
	private String providerResponse;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@OneToOne(mappedBy = "payment", cascade = CascadeType.ALL)
	private Refund refund;
}
