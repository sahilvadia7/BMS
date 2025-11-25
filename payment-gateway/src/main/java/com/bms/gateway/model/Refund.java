package com.bms.gateway.model;

import com.bms.gateway.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "refunds", indexes = {
		@Index(name = "idx_refund_payment_id", columnList = "payment_id")
})
public class Refund {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@OneToOne
	@JoinColumn(name = "payment_id", unique = true)
	private Payment payment;

	@Column(nullable = false)
	private BigDecimal refundAmount;

	@Column
	private String reason;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RefundStatus status;

	@Column(unique = true)
	private String idempotencyKey;

	private String providerRefundId;

	@Column(columnDefinition = "TEXT")
	private String providerResponse;

	@CreationTimestamp
	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;
}
