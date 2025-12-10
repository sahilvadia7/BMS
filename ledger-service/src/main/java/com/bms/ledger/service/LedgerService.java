package com.bms.ledger.service;

import com.bms.ledger.enums.LedgerType;
import com.bms.ledger.model.LedgerEntry;

import java.math.BigDecimal;

public interface LedgerService {
	BigDecimal getLatestBalance(String accountNumber);
	LedgerEntry processLedgerEntry(
			String transactionId,
			String accountNumber,
			BigDecimal amount,
			LedgerType type,
			String eventStep,
			String description,
			boolean isSuccess,
			String failureReason
	);
}
