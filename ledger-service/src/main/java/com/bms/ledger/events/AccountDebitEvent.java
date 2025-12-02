package com.bms.ledger.events;

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
public class AccountDebitEvent extends BaseEvent {
	private String transactionId;
	private String accountNumber;
	private BigDecimal amount;
	private String description;
	private LocalDateTime timestamp;
}