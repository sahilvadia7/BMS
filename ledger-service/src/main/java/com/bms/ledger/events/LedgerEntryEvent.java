package com.bms.ledger.events;

import com.bms.ledger.enums.LedgerType;
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