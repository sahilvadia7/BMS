package com.bms.transaction.events;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LedgerResultEvent {
	private String transactionId;
	private boolean success;
	private String message;
}
