package com.bms.transaction.model;

import com.bms.transaction.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction_outbox")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	private String aggregateId;

	private String eventType;

	@Lob
	@Basic(fetch = FetchType.EAGER)
	private String payload;

	@Enumerated(EnumType.STRING)
	private OutboxStatus status;

	private int retryCount;

	private LocalDateTime createdAt;
}