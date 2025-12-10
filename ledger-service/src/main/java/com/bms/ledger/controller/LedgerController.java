package com.bms.ledger.controller;

import com.bms.ledger.model.LedgerEntry;
import com.bms.ledger.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
public class LedgerController {

	private final LedgerRepository ledgerRepository;

	@GetMapping("/transaction")
	public Optional<LedgerEntry> getTransaction(
			@RequestParam String transactionId,
			@RequestParam String accountNumber) {
		return ledgerRepository.findByTransactionIdAndAccountNumber(transactionId, accountNumber);
	}

	@GetMapping("/account/{accountNumber}/transactions")
	public List<LedgerEntry> getTransactionsByAccount(@PathVariable String accountNumber) {
		return ledgerRepository.findByAccountNumberOrderByCreatedAtDesc(accountNumber);
	}

	@GetMapping("/account/{accountNumber}/balance")
	public BigDecimal getLatestBalance(@PathVariable String accountNumber) {
		return ledgerRepository.findTopByAccountNumberOrderByCreatedAtDesc(accountNumber)
				.map(LedgerEntry::getBalanceAfter)
				.orElse(BigDecimal.ZERO);
	}
}

