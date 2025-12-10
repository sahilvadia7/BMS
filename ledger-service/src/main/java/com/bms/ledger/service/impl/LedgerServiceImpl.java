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

	@Override
	public BigDecimal getLatestBalance(String accountNumber) {
		return ledgerRepository.findByAccountNumberOrderByCreatedAtDesc(accountNumber)
				.stream()
				.findFirst()
				.map(LedgerEntry::getBalanceAfter)
				.orElse(BigDecimal.ZERO);
	}

	/**
	 * APPLY BALANCE CHANGE ONLY ON SUCCESS EVENTS
	 */
	private BigDecimal applyBalanceChange(BigDecimal currentBalance,
										  LedgerType type,
										  BigDecimal amount) {

		switch (type) {
			case DEBIT_SUCCESS:
				return currentBalance.subtract(amount);

			case CREDIT_SUCCESS:
				return currentBalance.add(amount);

			case COMPENSATION_DEBIT:
				return currentBalance.subtract(amount);

			case COMPENSATION_CREDIT:
				return currentBalance.add(amount);

			default:
				// REQUESTED or FAILED events: no balance change
				return currentBalance;
		}
	}

	@Override
	public LedgerEntry processLedgerEntry(
			String transactionId,
			String accountNumber,
			BigDecimal amount,
			LedgerType type,
			String eventStep,
			String description,
			boolean isSuccess,
			String failureReason
	) {

		BigDecimal currentBalance = getLatestBalance(accountNumber);

		// FIXED: use only applyBalanceChange()
		BigDecimal newBalance = applyBalanceChange(currentBalance, type, amount);

		LedgerEntry entry = LedgerEntry.builder()
				.transactionId(transactionId)
				.accountNumber(accountNumber)
				.amount(amount)
				.type(type)
				.eventStep(eventStep)
				.description(description)
				.success(isSuccess)
				.failureReason(failureReason)
				.balanceAfter(newBalance)
				.build();

		ledgerRepository.save(entry);

		log.info("Ledger entry created: {}", entry);

		return entry;
	}
}
