package com.bms.ledger.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionFailedEvent extends BaseEvent {
	private String transactionId;
	private String reason;
	private LocalDateTime timestamp;
}
