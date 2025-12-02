package com.bms.transaction.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCreatedEvent extends BaseEvent {
	private String transactionId;
	private String sourceAccount;
	private String destinationAccount;
	private BigDecimal amount;
	private String type;
	private String description;
	private LocalDateTime timestamp;
}