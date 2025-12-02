package com.bms.transaction.events;

import com.bms.transaction.enums.LedgerType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LedgerEntryEvent {
	private String ledgerEntryId;
	private String transactionId;
	private String accountNumber;
	private BigDecimal amount;
	private LedgerType entryType;
	private String description;
	private LocalDateTime timestamp;
}