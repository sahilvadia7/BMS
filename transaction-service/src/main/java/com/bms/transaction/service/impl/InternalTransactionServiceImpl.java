package com.bms.transaction.service.impl;

import com.bms.transaction.dto.request.PaymentRequest;
import com.bms.transaction.dto.request.TransactionRequest;
import com.bms.transaction.dto.response.AccountResponseDTO;
import com.bms.transaction.dto.response.PaymentResponse;
import com.bms.transaction.dto.response.TransactionResponseDto;
import com.bms.transaction.enums.*;
import com.bms.transaction.enums.Currency;
import com.bms.transaction.exception.InvalidPinException;
import com.bms.transaction.exception.ResourceNotFoundException;
import com.bms.transaction.feing.AccountClient;
import com.bms.transaction.feing.CustomerClient;
import com.bms.transaction.feing.NotificationClient;
import com.bms.transaction.model.OutboxEvent;
import com.bms.transaction.model.Transaction;
import com.bms.transaction.producer.LedgerEventProducer;
import com.bms.transaction.repository.OutboxRepository;
import com.bms.transaction.repository.TransactionRepository;
import com.bms.transaction.service.InternalTransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class InternalTransactionServiceImpl implements InternalTransactionService {

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private AccountClient accountClient;

	@Autowired
	private OutboxRepository outboxRepository;

	@Autowired
	private LedgerEventProducer ledgerEventProducer;

	@Autowired
	private NotificationClient notificationClient;

	@Autowired
	private CustomerClient customerClient;

	@Value("${bank.account.number}")
	private String BANK_ACC_NUMBER;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private static final Map<TransactionType, BigDecimal> WEEKLY_LIMITS = Map.of(
			TransactionType.DEPOSIT, new BigDecimal("50000"),
			TransactionType.WITHDRAWAL, new BigDecimal("20000"),
			TransactionType.TRANSFER, new BigDecimal("20000"),
			TransactionType.LOAN_DISBURSEMENT, new BigDecimal("100000"),
			TransactionType.EMI_DEDUCTION, new BigDecimal("50000"),
			TransactionType.REFUND, new BigDecimal("100000"),
			TransactionType.CASH_DEPOSIT, new BigDecimal("300000"),
			TransactionType.CASH_WITHDRAWAL, new BigDecimal("200000")
	);

	@Override
	@Transactional
	public Object createTransaction(TransactionRequest request) {
		validateRequest(request);
		checkIdempotency(request);
		validateAccount(request.getAccountNumber());
		checkSufficientBalance(request.getAccountNumber(), request.getAmount());

		if (requiresDestinationAccount(request.getTransactionType())) {
			validateAccount(request.getDestinationAccountNumber());
		}

		if (isDebitType(request.getTransactionType())) {
			verifyAccountPin(request.getAccountNumber(),
					Integer.parseInt(Optional.ofNullable(request.getPin()).orElse("0")));
		}

		BigDecimal fee = calculateFee(request);
		Transaction txn = createPendingTransaction(request, fee);

		if (!request.getTransactionType().equals(TransactionType.EXTERNAL_TRANSFER)) {
			processInternalTransaction(txn);
			txn.setStatus(TransactionStatus.COMPLETED);
			txn.setCompletedAt(LocalDateTime.now());
			transactionRepository.save(txn);

			ledgerEventProducer.transactionCompleted(
					txn.getTransactionId(),
					txn.getAmount(),
					"Internal transaction completed Successfully"
			);

			return buildInternalResponse(txn);
		}

		return initiateExternalTransfer(txn, request);
	}

	private Transaction createPendingTransaction(TransactionRequest request, BigDecimal fee) {
		String requestHash = generateRequestHash(request);

		Transaction txn = Transaction.builder()
				.transactionId(UUID.randomUUID().toString())
				.accountNumber(request.getAccountNumber())
				.destinationAccountNumber(requiresDestinationAccount(request.getTransactionType()) ? request.getDestinationAccountNumber() : null)
				.transactionType(request.getTransactionType())
				.destinationBankCode(request.getDestinationBankCode())
				.branchCode(request.getBranchCode() != null ? request.getBranchCode() : "BMS0001")
				.gatewayProvider(request.getGatewayProvider())
				.amount(request.getAmount())
				.currency(request.getCurrency())
				.fee(fee)
				.chargeable(fee.signum() > 0)
				.status(TransactionStatus.PENDING)
				.description(request.getDescription())
				.initiatedBy(request.getInitiatedBy())
				.idempotencyKey(request.getIdempotencyKey())
				.channelReferenceId(request.getChannelReferenceId())
				.pinVerified(true)
				.requestHash(requestHash)
				.transactionDate(LocalDateTime.now())
				.build();

		Transaction savedTxn = transactionRepository.save(txn);

		registerInitialLedgerEvents(savedTxn);

		return savedTxn;
	}

	private void registerInitialLedgerEvents(Transaction txn) {
		Set<LedgerType> publishedTypes = new HashSet<>();

		switch (txn.getTransactionType()) {
			case DEPOSIT, CASH_DEPOSIT:
				publishLedgerOnce(txn.getAccountNumber(), txn.getAmount(), LedgerType.CREDIT_REQUESTED,
						txn.getTransactionId(), "Credit requested", publishedTypes);
				break;

			case WITHDRAWAL, CASH_WITHDRAWAL, EMI_DEDUCTION, PENALTY, TRANSFER:
				publishLedgerOnce(txn.getAccountNumber(), txn.getAmount(), LedgerType.DEBIT_REQUESTED,
						txn.getTransactionId(), "Debit requested", publishedTypes);

				if (txn.getTransactionType() == TransactionType.TRANSFER && txn.getDestinationAccountNumber() != null) {
					publishLedgerOnce(txn.getDestinationAccountNumber(), txn.getAmount(), LedgerType.CREDIT_REQUESTED,
							txn.getTransactionId(), "Credit requested", publishedTypes);
				}
				break;

			case LOAN_DISBURSEMENT, REFUND:
				publishLedgerOnce(txn.getDestinationAccountNumber(), txn.getAmount(), LedgerType.CREDIT_REQUESTED,
						txn.getTransactionId(), "Credit requested", publishedTypes);
				break;

			case FEE:
				publishLedgerOnce(txn.getAccountNumber(), txn.getAmount(), LedgerType.DEBIT_REQUESTED,
						txn.getTransactionId(), "Fee debit requested", publishedTypes);
				publishLedgerOnce(BANK_ACC_NUMBER, txn.getAmount(), LedgerType.CREDIT_REQUESTED,
						txn.getTransactionId(), "Fee credit requested", publishedTypes);
				break;

			case EXTERNAL_TRANSFER:
				// Only DEBIT_REQUESTED at creation, rest handled by Outbox scheduler when gateway responds
				publishLedgerOnce(txn.getAccountNumber(), txn.getAmount(), LedgerType.DEBIT_REQUESTED,
						txn.getTransactionId(), "External transfer debit requested", publishedTypes);
				break;

			case REVERSAL:
				handleReversalLedger(txn, publishedTypes);
				break;
		}
	}

	private void handleReversalLedger(Transaction txn, Set<LedgerType> publishedTypes) {
		Transaction original = transactionRepository
				.findByTransactionId(txn.getLinkedTransactionId())
				.orElseThrow(() -> new IllegalStateException("Original transaction not found"));

		publishLedgerOnce(original.getAccountNumber(), original.getAmount(), LedgerType.COMPENSATION_DEBIT,
				txn.getTransactionId(), "Reversal debit requested", publishedTypes);

		if (original.getDestinationAccountNumber() != null) {
			publishLedgerOnce(original.getDestinationAccountNumber(), original.getAmount(), LedgerType.COMPENSATION_CREDIT,
					txn.getTransactionId(), "Reversal credit requested", publishedTypes);
		}

		if (original.getFee() != null && original.getFee().signum() > 0) {
			publishLedgerOnce(original.getAccountNumber(), original.getFee(), LedgerType.COMPENSATION_CREDIT,
					txn.getTransactionId(), "Fee refund credit requested", publishedTypes);
			publishLedgerOnce(BANK_ACC_NUMBER, original.getFee(), LedgerType.COMPENSATION_DEBIT,
					txn.getTransactionId(), "Fee refund debit requested", publishedTypes);
		}
	}

	private void publishLedgerOnce(String account, BigDecimal amount, LedgerType type,
								   String txnId, String description, Set<LedgerType> publishedTypes) {
		if (amount == null || amount.signum() <= 0 || account == null || account.isEmpty()) return;

		if (!publishedTypes.contains(type)) {
			publishLedgerEntry(account, amount, type, txnId, description);
			publishedTypes.add(type);
		}
	}

	private void publishLedgerEntry(String account, BigDecimal amount, LedgerType type, String txnId, String description) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				try {
					switch (type) {
						case DEBIT_REQUESTED ->
								ledgerEventProducer.debitRequested(txnId, account, amount, description);
						case DEBIT_SUCCESS ->
								ledgerEventProducer.debitSuccess(txnId, account, amount, description);
						case DEBIT_FAILED ->
								ledgerEventProducer.debitFailed(txnId, account, amount, description, "Insufficient balance or system error");
						case CREDIT_REQUESTED ->
								ledgerEventProducer.creditRequested(txnId, account, amount, description);
						case CREDIT_SUCCESS ->
								ledgerEventProducer.creditSuccess(txnId, account, amount, description);
						case CREDIT_FAILED ->
								ledgerEventProducer.creditFailed(txnId, account, amount, description, "System error during credit");
						case COMPENSATION_DEBIT ->
								ledgerEventProducer.compensationDebit(txnId, account, amount, description, true, null);
						case COMPENSATION_CREDIT ->
								ledgerEventProducer.compensationCredit(txnId, account, amount, description, true, null);
						default -> log.warn("LedgerType {} not handled for txn {}", type, txnId);
					}
				} catch (Exception e) {
					log.error("Failed to publish ledger entry for txn {}: {}", txnId, e.getMessage());
				}
				log.info("Published LEDGER_ENTRY: txnId={}, account={}, type={}, amount={}", txnId, account, type, amount);
			}
		});
	}

	public void publishCompensationLedger(Transaction txn) {
		if (txn == null) return;
		publishCompensationLedger(txn.getAccountNumber(), txn.getAmount(), txn.getTransactionId());
	}

	public void publishCompensationLedger(String accountNumber, BigDecimal amount, String txnId) {
		if (accountNumber == null || amount == null || amount.signum() <= 0) {
			log.warn("Skipping compensation: invalid data account={}, amount={}", accountNumber, amount);
			return;
		}

		// 1) credit back sender (compensation credit)
		publishLedgerEntry(accountNumber, amount, LedgerType.COMPENSATION_CREDIT, txnId,
				"Compensation: refund to sender because external transfer failed");

		// 2) optional: create a receiver compensation/debit informational entry
		// (this doesn't change balances but records that receiver credit never happened)
		// If you want to record as COMPENSATION_DEBIT for receiver, ensure ledger consumer treats it as informational.
		// Only publish if destination exists in txn record (caller can pass receiver account if desired).
		// Here we do not touch destination unless caller provides it (use publishCompensationLedger(txn) for that).
	}

	private void processInternalTransaction(Transaction txn) {

		switch (txn.getTransactionType()) {
			case DEPOSIT, CASH_DEPOSIT ->
					creditAccount(txn.getAccountNumber(), txn.getAmount(), txn.getTransactionId());

			case WITHDRAWAL, CASH_WITHDRAWAL, EMI_DEDUCTION, PENALTY ->
					debitAccount(txn.getAccountNumber(), txn.getAmount(), txn.getTransactionId());

			case TRANSFER -> {
				debitAccount(txn.getAccountNumber(), txn.getAmount(), txn.getTransactionId());
				if (txn.getDestinationAccountNumber() != null) {
					creditAccount(txn.getDestinationAccountNumber(), txn.getAmount(), txn.getTransactionId());
				}
			}

			case LOAN_DISBURSEMENT, REFUND ->
					creditAccount(txn.getDestinationAccountNumber(), txn.getAmount(), txn.getTransactionId());

			case FEE ->
					applyFeeIfAny(txn);

			default ->
					log.warn("Internal transaction processing not defined for type {}", txn.getTransactionType());
		}

		applyFeeIfAny(txn);

		ledgerEventProducer.transactionCompleted(
				txn.getTransactionId(),
				txn.getAmount(),
				"Internal transaction completed successfully"
		);
	}

	private Object initiateExternalTransfer(Transaction txn, TransactionRequest request) {
		try {
			OutboxEvent outbox = OutboxEvent.builder()
					.id(UUID.randomUUID())
					.aggregateId(txn.getTransactionId())
					.aggregateType("Transaction")
					.eventType("EXTERNAL_TRANSFER")
					.payload(objectMapper.writeValueAsString(mapToPaymentRequest(request, txn.getTransactionId())))
					.status(OutboxStatus.PENDING)
					.retryCount(0)
					.createdAt(LocalDateTime.now())
					.build();

			outboxRepository.save(outbox);

			log.info("External transfer transaction saved to outbox: txnId={}", txn.getTransactionId());

		} catch (Exception e) {
			log.error("Failed to save external transfer to outbox: {}", e.getMessage());
			throw new IllegalStateException("External transfer initiation failed", e);
		}

		return buildInternalResponse(txn);
	}

	public void debitAccount(String account, BigDecimal amount, String txnId) {

		publishLedgerEntry(account, amount, LedgerType.DEBIT_REQUESTED, txnId, "Debit requested");

		try {
			accountClient.updateBalance(account, amount, "WITHDRAW");

			publishLedgerEntry(account, amount, LedgerType.DEBIT_SUCCESS, txnId, "Debit successful");

			Transaction txn = transactionRepository.findByTransactionId(txnId).orElse(null);
			if (txn != null) sendTransactionAlert(txn);

		} catch (Exception e) {
			publishLedgerEntry(account, amount, LedgerType.DEBIT_FAILED, txnId, "Debit failed: " + e.getMessage());

			ledgerEventProducer.transactionFailed(
					txnId,
					"Debit operation failed",
					e.getMessage()
			);

			throw new IllegalStateException("Debit failed for account " + account, e);
		}
	}

	public void creditAccount(String account, BigDecimal amount, String txnId) 	{
		publishLedgerEntry(account, amount, LedgerType.CREDIT_REQUESTED, txnId, "Credit requested");

		try {
			accountClient.updateBalance(account, amount, "DEPOSIT");

			publishLedgerEntry(account, amount, LedgerType.CREDIT_SUCCESS, txnId, "Credit successful");

			Transaction txn = transactionRepository.findByTransactionId(txnId).orElse(null);
			if (txn != null) sendTransactionAlert(txn);

		} catch (Exception e) {
			publishLedgerEntry(account, amount, LedgerType.CREDIT_FAILED, txnId, "Credit failed: " + e.getMessage());

			ledgerEventProducer.transactionFailed(
					txnId,
					"Credit operation failed",
					e.getMessage()
			);

			throw new IllegalStateException("Credit failed for account " + account, e);
		}
	}

	private void applyFeeIfAny(Transaction txn) {
		if (txn.getFee() != null && txn.getFee().signum() > 0) {
			Transaction feeTxn = Transaction.builder()
					.transactionId(UUID.randomUUID().toString())
					.accountNumber(txn.getAccountNumber())
					.destinationAccountNumber(BANK_ACC_NUMBER)
					.transactionType(TransactionType.FEE)
					.amount(txn.getFee())
					.currency(txn.getCurrency())
					.status(TransactionStatus.PENDING)
					.linkedTransactionId(txn.getTransactionId())
					.description("Fee applied")
					.transactionDate(LocalDateTime.now())
					.build();
			transactionRepository.save(feeTxn);

			debitAccount(txn.getAccountNumber(), txn.getFee(), feeTxn.getTransactionId());
			creditAccount(BANK_ACC_NUMBER, txn.getFee(), feeTxn.getTransactionId());

			feeTxn.setStatus(TransactionStatus.COMPLETED);
			transactionRepository.save(feeTxn);
		}
	}

	private void checkSufficientBalance(String account, BigDecimal amount) {
		BigDecimal balance = Optional.ofNullable(accountClient.getBalance(account)).orElse(BigDecimal.ZERO);
		if (balance.compareTo(amount) < 0)
			throw new IllegalStateException("Insufficient balance for account: " + account);
	}

	private boolean isDebitType(TransactionType type) {
		return Set.of(TransactionType.WITHDRAWAL, TransactionType.CASH_WITHDRAWAL,
				TransactionType.TRANSFER, TransactionType.EMI_DEDUCTION, TransactionType.PENALTY).contains(type);
	}

	private void validateRequest(TransactionRequest req) {
		if (req.getAccountNumber() == null) throw new IllegalArgumentException("Source account required");
		if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0)
			throw new IllegalArgumentException("Invalid amount");
		if (req.getTransactionType() == null) throw new IllegalArgumentException("Transaction type required");
		if (req.getIdempotencyKey() == null) throw new IllegalArgumentException("Idempotency Key required");
		if (req.getCurrency() == null) req.setCurrency(Currency.INR);
	}

	private void checkIdempotency(TransactionRequest req) {
		transactionRepository.findByIdempotencyKey(req.getIdempotencyKey()).ifPresent(existing -> {
			if (!existing.getRequestHash().equals(generateRequestHash(req)))
				throw new IllegalStateException("Idempotency key reused with different payload");
			throw new IllegalStateException("Duplicate transaction");
		});
	}

	private void validateAccount(String accountNumber) {
		if (!Optional.ofNullable(accountClient.accountExists(accountNumber)).orElse(false)) {
			throw new ResourceNotFoundException("Account not found: " + accountNumber);
		}
	}

	private void verifyAccountPin(String account, int pin) {
		if (!Optional.ofNullable(accountClient.verifyAccountPin(account, pin)).orElse(false)) {
			throw new InvalidPinException("Invalid PIN");
		}
	}

	private BigDecimal calculateFee(TransactionRequest req) {
		BigDecimal fee = switch (req.getTransactionType()) {
			case CASH_WITHDRAWAL -> req.getAmount().multiply(new BigDecimal("0.005"));
			case CASH_DEPOSIT -> req.getAmount().multiply(new BigDecimal("0.002"));
			default -> BigDecimal.ZERO;
		};
		return fee.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	private String generateRequestHash(TransactionRequest req) {
		return DigestUtils.sha256Hex(
				(req.getAccountNumber() == null ? "" : req.getAccountNumber()) +
						(req.getDestinationAccountNumber() == null ? "" : req.getDestinationAccountNumber()) +
						req.getAmount() + req.getTransactionType() + req.getIdempotencyKey()
		);
	}

	private boolean requiresDestinationAccount(TransactionType type) {
		return Set.of(TransactionType.TRANSFER, TransactionType.EXTERNAL_TRANSFER,
				TransactionType.REFUND, TransactionType.LOAN_DISBURSEMENT, TransactionType.REVERSAL).contains(type);
	}

	private void moveFromVault(Transaction txn) {
		log.info("Cash moved from vault for txn {}", txn.getTransactionId());
	}

	private void postToLoanModule(Transaction txn) {
		log.info("Posting EMI deduction for txn {}", txn.getTransactionId());
	}

	private void postToPenaltyWallet(Transaction txn) {
		log.info("Posting penalty for txn {}", txn.getTransactionId());
	}

	public void sendTransactionAlert(Transaction txn) {
		try {
			AccountResponseDTO accountResponse = accountClient.getAccountByNumber(txn.getAccountNumber());

			Map<String, Object> customer = customerClient.getLimitedInfoByCif(accountResponse.getCifNumber());
			notificationClient.sendTransactionAlert(txn, (String) customer.get("email"));


			notificationClient.sendTransactionAlert(txn, (String) customer.get("email"));
			log.info("Transaction alert sent successfully for txnId={}, response={}", txn.getTransactionId());
		} catch (Exception e) {
			log.error("Failed to send transaction alert for txnId={}: {}", txn.getTransactionId(), e.getMessage());
		}
	}

	private PaymentRequest mapToPaymentRequest(TransactionRequest request, String txnId) {
		if (!request.getTransactionType().equals(TransactionType.EXTERNAL_TRANSFER)) {
			throw new IllegalArgumentException("Invalid mapping for non-external transaction");
		}
		return PaymentRequest.builder()
				.sourceAccount(request.getAccountNumber())
				.destinationAccount(request.getDestinationAccountNumber())
				.destinationBankCode(request.getDestinationBankCode())
				.amount(request.getAmount())
				.currency(request.getCurrency())
				.transactionId(txnId)
				.description(request.getDescription())
				.build();
	}

	private TransactionResponseDto buildInternalResponse(Transaction txn) {
		return new TransactionResponseDto(
				txn.getTransactionId(),
				"",
				txn.getAccountNumber(),
				txn.getDestinationAccountNumber(),
				txn.getTransactionType().name(),
				txn.getAmount(),
				txn.getTransactionDate(),
				txn.getCurrency().name(),
				"INTERNAL",
				txn.getStatus().name(),
				txn.getDescription(),
				txn.getTransactionId()
		);
	}
}
