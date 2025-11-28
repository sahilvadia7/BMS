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
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String aggregateId;
	private String eventType;
	private String payload;
	private LocalDateTime createdAt;

	@Enumerated(EnumType.STRING)
	private OutboxStatus status;

	private int retryCount;
}
