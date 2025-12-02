package com.bms.ledger.service.impl;

import com.bms.ledger.enums.LedgerType;
import com.bms.ledger.model.LedgerEntry;
import com.bms.ledger.repository.LedgerRepository;
import com.bms.ledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerServiceImpl implements LedgerService {

	private final LedgerRepository ledgerRepository;

	public BigDecimal getLatestBalance(String accountNumber) {

		return ledgerRepository.findByAccountNumberOrderByCreatedAtDesc(accountNumber)
				.stream()
				.findFirst()
				.map(LedgerEntry::getBalanceAfter)
				.orElse(BigDecimal.ZERO);
	}

	public LedgerEntry processLedgerEntry(
			String transactionId,
			String accountNumber,
			BigDecimal amount,
			LedgerType type,
			String description,
			String transactionType
	) {

		BigDecimal currentBalance = getLatestBalance(accountNumber);

		BigDecimal newBalance = type == LedgerType.DEBIT
				? currentBalance.subtract(amount)
				: currentBalance.add(amount);

		LedgerEntry entry = LedgerEntry.builder()
				.transactionId(transactionId)
				.accountNumber(accountNumber)
				.amount(amount)
				.type(type)
				.description(description)
				.transactionType(transactionType)
				.balanceAfter(newBalance)
				.build();

		ledgerRepository.save(entry);

		log.info("Ledger entry saved: {}", entry);

		return entry;
	}
}

