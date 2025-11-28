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
import com.bms.transaction.feing.PaymentClient;
import com.bms.transaction.model.OutboxEvent;
import com.bms.transaction.model.Transaction;
import com.bms.transaction.repository.OutboxRepository;
import com.bms.transaction.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.http.codec.ServerSentEvent.builder;

/**
 * NOTE:
 * - This implementation persists an OutboxEvent instead of calling the payment gateway directly.
 * - Create a separate OutboxEvent entity + OutboxRepository (JPA) to back this.
 *
 * OutboxEvent fields expected (example):
 *  - id (String/UUID)
 *  - aggregateId (transactionId)
 *  - eventType (e.g. EXTERNAL_TRANSFER_INITIATED)
 *  - payloadJson (text)
 *  - status (PENDING, SENT, FAILED)
 *  - attempts (int)
 *  - nextAttemptAt (LocalDateTime)
 *  - createdAt (LocalDateTime)
 *
 * A scheduler should poll OutboxRepository for PENDING/RETRY events and call payment service.
 */
@Service
@Slf4j
@AllArgsConstructor
public class InternalTransactionServiceImpl implements com.bms.transaction.service.InternalTransactionService {

	private final TransactionRepository transactionRepository;
	private final AccountClient accountClient;
	private final PaymentClient paymentClient;
	private final OutboxRepository outboxRepository;

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

	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Creates transaction. For EXTERNAL_TRANSFER this method will:
	 *  - persist txn (PENDING)
	 *  - persist outbox event (PENDING)
	 *  - return an immediate Processing response
	 *
	 * Payment gateway call is delegated to Outbox Scheduler (separate component).
	 */
	@Override
	@Transactional
	public Object createTransaction(TransactionRequest request) {

		validateRequest(request);
		checkIdempotency(request);
		validateAccount(request.getAccountNumber());

		if (requiresDestinationAccount(request.getTransactionType())) {
			validateDestinationAccount(request.getDestinationAccountNumber());
		}

		// Verify PIN if needed (debit operations)
		if (isDebitType(request.getTransactionType())) {
			verifyAccountPin(request.getAccountNumber(), Integer.parseInt(Optional.ofNullable(request.getPin()).orElse("0")));
		}

		BigDecimal fee = calculateFee(request);
		Transaction txn = createPendingTransaction(request, fee);

		// INTERNAL TRANSACTION: handle synchronously (ACID within local DB and reliance on account-service atomic ops)
		if (!request.getTransactionType().equals(TransactionType.EXTERNAL_TRANSFER)) {
			processInternalOperation(txn);
			txn.setStatus(TransactionStatus.COMPLETED);
			txn.setCompletedAt(LocalDateTime.now());
			transactionRepository.save(txn);
			return buildInternalResponse(txn);
		}

		// EXTERNAL TRANSACTION: create outbox event (do not call external service here)
		try {
			PaymentRequest paymentRequest = mapToPaymentRequest(request, txn.getTransactionId());

			// create outbox event payload (serialized)
			String payloadJson = objectMapper.writeValueAsString(paymentRequest);

			OutboxEvent outbox = OutboxEvent.builder()
					.id(UUID.randomUUID())
					.aggregateId(txn.getTransactionId())
					.eventType("EXTERNAL_TRANSFER_INITIATED")
					.payload(payloadJson)
					.status(OutboxStatus.PENDING)
					.retryCount(0)
					.createdAt(LocalDateTime.now())
					.build();

			outboxRepository.save(outbox);

			txn.setStatus(TransactionStatus.PROCESSING);
			txn.setGatewayProvider(request.getGatewayProvider());
			txn.setTransactionDate(LocalDateTime.now());
			transactionRepository.save(txn);

			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					log.info("Outbox created after commit: outboxId={} txnId={}", outbox.getId(), txn.getTransactionId());
				}
			});

			PaymentResponse processing = PaymentResponse.builder()
					.transactionId(txn.getTransactionId())
					.externalReferenceId(null)
					.status(TransactionStatus.PROCESSING)
					.amount(txn.getAmount())
					.destinationBankCode(request.getDestinationBankCode())
					.initiatedAt(LocalDateTime.now())
					.completedAt(null)
					.failureReason(null)
					.build();

			return processing;

		} catch (Exception e) {
			log.error("Failed creating outbox for external transfer: {}", e.getMessage(), e);
			txn.setStatus(TransactionStatus.FAILED);
			txn.setFailureReason(e.getMessage());
			transactionRepository.save(txn);

			return PaymentResponse.failed(txn);
		}
	}


	private Transaction createPendingTransaction(TransactionRequest request, BigDecimal fee) {
		String requestHash = generateRequestHash(request);

		String destinationAcc = switch (request.getTransactionType()) {
			case TRANSFER -> request.getDestinationAccountNumber();
			case EMI_DEDUCTION -> null;
			case WITHDRAWAL, DEPOSIT, LOAN_DISBURSEMENT, REFUND -> null;
			case CASH_DEPOSIT -> null;
			case CASH_WITHDRAWAL -> null;
			case PENALTY -> null;
			case REVERSAL -> null;
			case EXTERNAL_TRANSFER -> request.getDestinationAccountNumber();
			case FEE -> "BANK_ACCOUNT";
		};

		Transaction txn = Transaction.builder()
				.transactionId(UUID.randomUUID().toString())
				.accountNumber(request.getAccountNumber())
				.destinationAccountNumber(destinationAcc)
				.transactionType(request.getTransactionType())
				.amount(request.getAmount())
				.currency(request.getCurrency())
				.fee(fee)
				.chargeable(fee.compareTo(BigDecimal.ZERO) > 0)
				.status(TransactionStatus.PENDING)
				.description(request.getDescription())
				.initiatedBy(request.getInitiatedBy())
				.idempotencyKey(request.getIdempotencyKey())
				.channelReferenceId(request.getChannelReferenceId())
				.branchCode(request.getBranchCode())
				.pinVerified(true)
				.requestHash(requestHash)
				.transactionDate(LocalDateTime.now())
				.build();

		return transactionRepository.save(txn);
	}

	private void processInternalOperation(Transaction txn) {
		BigDecimal total = txn.getAmount().add(Optional.ofNullable(txn.getFee()).orElse(BigDecimal.ZERO));
		String source = txn.getAccountNumber();
		String dest = txn.getDestinationAccountNumber();

		switch (txn.getTransactionType()) {
			case WITHDRAWAL -> {
				checkSufficientBalance(source, total);
				debitAccount(source, total, txn.getTransactionId());
				if (txn.getFee().signum() > 0) applyFee(source, txn);
			}
			case CASH_WITHDRAWAL -> {
				checkSufficientBalance(source, total);
				debitAccount(source, total, txn.getTransactionId());
				moveToVault(txn);
				if (txn.getFee().signum() > 0) applyFee(source, txn);
			}
			case CASH_DEPOSIT -> {
				moveFromVault(txn);
				creditAccount(source, txn.getAmount(), txn.getTransactionId());
			}
			case TRANSFER -> {
				validateAccount(dest);
				checkSufficientBalance(source, total);
				debitAccount(source, total, txn.getTransactionId());
				creditAccount(dest, txn.getAmount(), txn.getTransactionId());
				if (txn.getFee().signum() > 0) applyFee(source, txn);
			}
			case DEPOSIT -> {
				creditAccount(source, txn.getAmount(), txn.getTransactionId());
			}
			case LOAN_DISBURSEMENT, REFUND -> {
				validateAccount(dest);
				creditAccount(dest, txn.getAmount(), txn.getTransactionId());
			}
			case EMI_DEDUCTION -> {
				checkSufficientBalance(source, total);
				debitAccount(source, total, txn.getTransactionId());
				postToLoanModule(txn);
				if (txn.getFee().signum() > 0) applyFee(source, txn);
			}
			case PENALTY -> {
				checkSufficientBalance(source, txn.getAmount());
				debitAccount(source, txn.getAmount(), txn.getTransactionId());
				postToPenaltyWallet(txn);
			}
			case REVERSAL -> reverseTransaction(txn);
			default -> throw new IllegalStateException("Unsupported internal operation: " + txn.getTransactionType());
		}
	}

	private void debitAccount(String acc, BigDecimal amount, String ref) {
		accountClient.updateBalance(acc, amount, "WITHDRAW");
		log.info("Debited {} from {} (Ref: {})", amount, acc, ref);
	}

	private void creditAccount(String acc, BigDecimal amount, String ref) {
		accountClient.updateBalance(acc, amount, "DEPOSIT");
		log.info("Credited {} to {} (Ref: {})", amount, acc, ref);
	}

	private void moveToVault(Transaction txn) { log.info("Moving {} to bank vault", txn.getAmount()); }

	private void moveFromVault(Transaction txn) { log.info("Cash deposit moved from vault"); }

	private void postToLoanModule(Transaction txn) { log.info("EMI applied for txn {}", txn.getTransactionId()); }

	private void reverseTransaction(Transaction reversalTxn) {
		Transaction original = transactionRepository
				.findByTransactionId(reversalTxn.getLinkedTransactionId())
				.orElseThrow(() -> new IllegalStateException("Original txn not found"));

		if (original.getStatus() != TransactionStatus.COMPLETED)
			throw new IllegalStateException("Only completed txns can be reversed");

		if (transactionRepository.existsByLinkedTransactionId(original.getTransactionId()))
			throw new IllegalStateException("This txn is already reversed");

		String source = original.getAccountNumber();
		String dest = original.getDestinationAccountNumber();
		BigDecimal amount = original.getAmount();

		log.info("Starting reversal for txn {} -> original {}", reversalTxn.getTransactionId(), original.getTransactionId());

		switch (original.getTransactionType()) {

			case DEPOSIT -> {
				checkSufficientBalance(source, amount);
				debitAccount(source, amount, reversalTxn.getTransactionId());
			}

			case WITHDRAWAL -> {
				creditAccount(source, amount, reversalTxn.getTransactionId());
			}

			case TRANSFER -> {
				creditAccount(source, amount, reversalTxn.getTransactionId());
				checkSufficientBalance(dest, amount);
				debitAccount(dest, amount, reversalTxn.getTransactionId());
			}

			case CASH_DEPOSIT -> {
				checkSufficientBalance(source, amount);
				debitAccount(source, amount, reversalTxn.getTransactionId());
			}

			case CASH_WITHDRAWAL -> {
				creditAccount(source, amount, reversalTxn.getTransactionId());
			}

			case EMI_DEDUCTION -> {
				creditAccount(source, amount, reversalTxn.getTransactionId());
			}

			case PENALTY -> {
				creditAccount(source, amount, reversalTxn.getTransactionId());

				String penaltyAcc = "AC_PENALTY_WALLET";
				BigDecimal walletBal = accountClient.getBalance(penaltyAcc).getBody();
				if (walletBal.compareTo(amount) < 0)
					throw new IllegalStateException("Penalty wallet insufficient to reverse");

				debitAccount(penaltyAcc, amount, reversalTxn.getTransactionId());
			}

			case LOAN_DISBURSEMENT -> {
				checkSufficientBalance(dest, amount);
				debitAccount(dest, amount, reversalTxn.getTransactionId());
			}

			case REFUND -> {
				checkSufficientBalance(dest, amount);
				debitAccount(dest, amount, reversalTxn.getTransactionId());
			}

			default -> throw new IllegalStateException("Reversal not supported for: " + original.getTransactionType());
		}

		if (original.getFee() != null && original.getFee().signum() > 0) {
			reverseFee(original, reversalTxn);
		}

		log.info("Reversal completed for txn {}", reversalTxn.getTransactionId());
	}

	private void reverseFee(Transaction original, Transaction reversalTxn) {

		String feeAcc = "AC_SYS_FEE";
		BigDecimal fee = original.getFee();

		BigDecimal bal = accountClient.getBalance(feeAcc).getBody();
		if (bal.compareTo(fee) < 0)
			throw new IllegalStateException("System fee account cannot cover fee reversal");

		debitAccount(feeAcc, fee, reversalTxn.getTransactionId());
		creditAccount(original.getAccountNumber(), fee, reversalTxn.getTransactionId());

		Transaction feeRev = Transaction.builder()
				.transactionId(UUID.randomUUID().toString())
				.accountNumber(feeAcc)
				.destinationAccountNumber(original.getAccountNumber())
				.transactionType(TransactionType.FEE)
				.amount(fee)
				.currency(Currency.INR)
				.linkedTransactionId(original.getTransactionId())
				.description("Reversal Fee Refund")
				.transactionDate(LocalDateTime.now())
				.status(TransactionStatus.COMPLETED)
				.build();

		transactionRepository.save(feeRev);
	}

	private void applyFee(String source, Transaction parentTxn) {
		Transaction feeTxn = Transaction.builder()
				.transactionId(UUID.randomUUID().toString())
				.accountNumber(source)
				.destinationAccountNumber("AC_SYS_FEE")
				.transactionType(TransactionType.FEE)
				.amount(parentTxn.getFee())
				.currency(Currency.INR)
				.status(TransactionStatus.COMPLETED)
				.linkedTransactionId(parentTxn.getTransactionId())
				.description("Fee...")
				.transactionDate(LocalDateTime.now())
				.build();

		transactionRepository.save(feeTxn);
		accountClient.updateBalance("AC_SYS_FEE", parentTxn.getFee(), "DEPOSIT");
	}

	private void checkSufficientBalance(String account, BigDecimal amt) {
		BigDecimal bal = accountClient.getBalance(account).getBody();
		if (bal.compareTo(amt) < 0)
			throw new IllegalStateException("Insufficient balance");
	}

	private boolean isDebitType(TransactionType t) {
		return List.of(TransactionType.WITHDRAWAL, TransactionType.CASH_WITHDRAWAL,
				TransactionType.TRANSFER, TransactionType.EMI_DEDUCTION, TransactionType.PENALTY).contains(t);
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
		Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(req.getIdempotencyKey());
		if (existing.isPresent()) {
			String hash = generateRequestHash(req);
			if (!existing.get().getRequestHash().equals(hash))
				throw new IllegalStateException("Idempotency key reused with different payload");
			throw new IllegalStateException("Duplicate transaction");
		}
	}

	private void postToPenaltyWallet(Transaction txn) { log.info("Penalty posted for txn {}", txn.getTransactionId()); }

	private void validateAccount(String accountNumber) {
		if (Boolean.FALSE.equals(accountClient.accountExists(accountNumber).getBody())) {
			throw new ResourceNotFoundException("Account not found: " + accountNumber);
		}
	}

	private boolean requiresDestinationAccount(TransactionType type) {
		return type == TransactionType.TRANSFER ||
				type == TransactionType.EXTERNAL_TRANSFER ||
				type == TransactionType.REFUND ||
				type == TransactionType.LOAN_DISBURSEMENT ||
				type == TransactionType.REVERSAL;
	}

	private void validateDestinationAccount(String destinationAccountNumber) {
		if (destinationAccountNumber == null || destinationAccountNumber.isEmpty()) {
			throw new IllegalArgumentException("Destination account number is missing");
		}

		AccountResponseDTO account = accountClient
				.getAccountByNumber(destinationAccountNumber)
				.getBody();

		if (account == null) {
			throw new ResourceNotFoundException(
					"Destination account not found: " + destinationAccountNumber);
		}
	}

	private void verifyAccountPin(String acc, int pin) {
		ResponseEntity<Boolean> res = accountClient.verifyAccountPin(acc, pin);
		if (!Boolean.TRUE.equals(res.getBody())) throw new InvalidPinException("Invalid PIN");
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

	private PaymentRequest mapToPaymentRequest(TransactionRequest request, String txnId) {

		if (!request.getTransactionType().equals(TransactionType.EXTERNAL_TRANSFER)) {
			throw new IllegalArgumentException("Invalid mapping call for non-external transaction");
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
