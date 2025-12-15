package com.bms.transaction.service.scheduler;

import com.bms.transaction.dto.request.PaymentRequest;
import com.bms.transaction.dto.response.PaymentResponse;
import com.bms.transaction.enums.LedgerType;
import com.bms.transaction.enums.OutboxStatus;
import com.bms.transaction.enums.TransactionStatus;
import com.bms.transaction.enums.TransactionType;
import com.bms.transaction.feing.PaymentClient;
import com.bms.transaction.model.OutboxEvent;
import com.bms.transaction.model.Transaction;
import com.bms.transaction.producer.LedgerEventProducer;
import com.bms.transaction.repository.LedgerRepository;
import com.bms.transaction.repository.OutboxRepository;
import com.bms.transaction.repository.TransactionRepository;
import com.bms.transaction.service.InternalTransactionService;
import com.bms.transaction.service.impl.InternalTransactionServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryTransactionScheduler {

	private final OutboxRepository outboxRepository;
	private final TransactionRepository transactionRepository;
	private final PaymentClient paymentClient;
	private final InternalTransactionServiceImpl transactionService;
	private final LedgerRepository ledgerRepository;
	private final InternalTransactionService internalTransactionService;
	private final LedgerEventProducer ledgerEventProducer;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private static final int MAX_RETRY = 5;

	@Scheduled(fixedDelay = 5000)
	@Transactional
	public void processOutboxEvents() {

		List<OutboxEvent> pendingEvents =
				outboxRepository.findTop10ByStatusOrderByCreatedAt(OutboxStatus.PENDING);

		if (pendingEvents.isEmpty()) return;

		log.info("Processing {} outbox events", pendingEvents.size());

		for (OutboxEvent event : pendingEvents) {

			try {
				processEvent(event);

			} catch (Exception e) {
				log.error("Error processing Outbox event {}: {}", event.getId(), e.getMessage());

				boolean permanentlyFailed = ledgerEventProducer.transactionFailed(event.getAggregateId(), "Error processing while Outbox event" , e.getMessage());

				if (permanentlyFailed) {
					log.error("EVENT {} permanently failed — triggering compensation.", event.getId());
					failTransactionWithCompensation(event, e.getMessage());
				}
			}
		}
	}

	@Transactional
	public void processEvent(OutboxEvent event) {

		log.info("Processing Outbox Event ID={}, type={}", event.getId(), event.getEventType());

		PaymentRequest request = parsePayload(event.getPayload());

		PaymentResponse response;
		try {
			response = paymentClient.initiatePayment(request);
			log.info("External Response: {}", response);
		} catch (Exception e) {
			throw new RuntimeException("Payment Gateway Call Failed: " + e.getMessage());
		}

		Transaction txn = transactionRepository.findByTransactionId(event.getAggregateId())
				.orElseThrow(() -> new RuntimeException("Transaction not found"));

		if (response.getStatus() == TransactionStatus.COMPLETED) {

			markTransactionSuccess(txn, response);

			event.setStatus(OutboxStatus.SENT);
			outboxRepository.save(event);

			log.info("External Payment SUCCESS → Ledger publishing for txn {}", txn.getTransactionId());

			transactionService.sendTransactionAlert(txn);

		} else {

			boolean permanentlyFailed = ledgerEventProducer.transactionFailed(event.getAggregateId(), "External Payment FAILED" , response.getFailureReason());

			log.warn("External Payment FAILED → txnId={} reason={}", txn.getTransactionId(), response.getFailureReason());

			if (permanentlyFailed) {
				failTransactionWithCompensation(event, response.getFailureReason());
			}
		}
	}

	private PaymentRequest parsePayload(String payloadJson) {
		try {
			return objectMapper.readValue(payloadJson, PaymentRequest.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse Outbox Payload: " + e.getMessage());
		}
	}

	private void markTransactionSuccess(Transaction txn, PaymentResponse response) {

		txn.setExternalReferenceId(response.getExternalReferenceId());
		txn.setGatewayAcknowledgedAt(LocalDateTime.now());
		transactionRepository.save(txn);

		if (txn.getTransactionType() == TransactionType.EXTERNAL_TRANSFER) {

			internalTransactionService.debitAccount(
					txn.getAccountNumber(),
					txn.getAmount(),
					txn.getTransactionId()
			);

			internalTransactionService.creditAccount(
					txn.getDestinationAccountNumber(),
					txn.getAmount(),
					txn.getTransactionId()
			);
		}

		txn.setStatus(TransactionStatus.COMPLETED);
		txn.setCompletedAt(LocalDateTime.now());
		transactionRepository.save(txn);

		ledgerEventProducer.transactionCompleted(txn.getTransactionId(), txn.getAmount(), "Transaction completed successfully");

		internalTransactionService.sendTransactionAlert(txn);
	}

	private boolean handleFailure(OutboxEvent event, String error) {

		int retries = event.getRetryCount() + 1;
		event.setRetryCount(retries);

		if (retries >= MAX_RETRY) {
			event.setStatus(OutboxStatus.FAILED);
			outboxRepository.save(event);

			Transaction txn = transactionRepository.findByTransactionId(event.getAggregateId())
					.orElse(null);
			if (txn != null) {
				ledgerEventProducer.transactionFailed(txn.getTransactionId(), "Transaction permanently failed", error);
			}

			return true;
		}

		event.setStatus(OutboxStatus.PENDING);
		outboxRepository.save(event);
		return false;
	}

	/**
	 * Applies compensation ONLY IF debit was actually completed.
	 */
	private void failTransactionWithCompensation(OutboxEvent event, String error) {

		Transaction txn = transactionRepository.findByTransactionId(event.getAggregateId())
				.orElseThrow(() -> new RuntimeException("Transaction not found"));

		txn.setLinkedTransactionId(txn.getExternalReferenceId());
		txn.setStatus(TransactionStatus.FAILED);
		txn.setFailureReason(error);
		txn.setCompletedAt(LocalDateTime.now());
		transactionRepository.save(txn);

		if (txn.getTransactionType() != TransactionType.EXTERNAL_TRANSFER) {
			log.info("No compensation for txn {} (not an EXTERNAL_TRANSFER).", txn.getTransactionId());
			return;
		}

		boolean debitExecuted = isDebitCompleted(txn.getTransactionId());

		if (!debitExecuted) {
			log.warn("NO COMPENSATION applied because DEBIT never completed for txn {}", txn.getTransactionId());
			return;
		}

		log.warn("Starting COMPENSATION → debit was completed but credit failed for txn {}", txn.getTransactionId());

		try {
			transactionService.publishCompensationLedger(txn);

			txn.setStatus(TransactionStatus.COMPENSATED);
			txn.setCompensatedAt(LocalDateTime.now());
			txn.setLinkedTransactionId(txn.getTransactionId());
			transactionRepository.save(txn);

			internalTransactionService.creditAccount(txn.getAccountNumber(),txn.getAmount(),txn.getTransactionId());

			transactionService.sendTransactionAlert(txn);
			log.info("COMPENSATION SUCCESS for txn {}", txn.getTransactionId());

		} catch (Exception e) {
			log.error("COMPENSATION FAILED for txn {} -> {}", txn.getTransactionId(), e.getMessage());
		}
	}

	/**
	 * Checks if DEBIT_SUCCESS exists in Ledger for this transaction.
	 */
	public boolean isDebitCompleted(String txnId) {
		return ledgerRepository.existsByTransactionIdAndType(txnId, LedgerType.DEBIT_SUCCESS);
	}
}
