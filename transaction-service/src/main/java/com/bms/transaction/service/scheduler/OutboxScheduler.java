package com.bms.transaction.service.scheduler;

import com.bms.transaction.dto.request.PaymentRequest;
import com.bms.transaction.dto.response.PaymentResponse;
import com.bms.transaction.enums.OutboxStatus;
import com.bms.transaction.enums.TransactionStatus;
import com.bms.transaction.feing.PaymentClient;
import com.bms.transaction.model.OutboxEvent;
import com.bms.transaction.model.Transaction;
import com.bms.transaction.repository.OutboxRepository;
import com.bms.transaction.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

	private final OutboxRepository outboxRepository;
	private final TransactionRepository transactionRepository;
	private final PaymentClient paymentClient;

	private static final int MAX_RETRY = 5;

	@Scheduled(fixedDelay = 3000)
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
				handleFailure(event, e.getMessage());
			}
		}
	}

	private void processEvent(OutboxEvent event) {

		log.info("Processing Outbox Event ID={}, type={}", event.getId(), event.getEventType());

		// Parse JSON into PaymentRequest
		PaymentRequest request = parsePayload(event.getPayload());

		// Call external payment API
		PaymentResponse response = paymentClient.initiatePayment(request);

		Transaction txn = transactionRepository.findByTransactionId(event.getAggregateId())
				.orElseThrow(() -> new RuntimeException("Transaction not found"));

		if (response.getStatus() == TransactionStatus.COMPLETED) {

			txn.setStatus(TransactionStatus.COMPLETED);
			txn.setExternalReferenceId(response.getExternalReferenceId());
			txn.setCompletedAt(LocalDateTime.now());
			transactionRepository.save(txn);

			event.setStatus(OutboxStatus.SENT);
			outboxRepository.save(event);

			log.info("External Payment Success → txnId={} marked COMPLETED", txn.getTransactionId());
		} else {

			// Payment Failed
			handleFailure(event, response.getFailureReason());

			txn.setStatus(TransactionStatus.FAILED);
			txn.setFailureReason(response.getFailureReason());
			txn.setCompletedAt(LocalDateTime.now());
			transactionRepository.save(txn);

			log.warn("External Payment FAILED → txnId={} marked FAILED", txn.getTransactionId());
		}
	}

	private PaymentRequest parsePayload(String payloadJson) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(payloadJson, PaymentRequest.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse Outbox Payload: " + e.getMessage());
		}
	}

	private void handleFailure(OutboxEvent event, String error) {
		int retries = event.getRetryCount() + 1;

		if (retries >= MAX_RETRY) {
			event.setStatus(OutboxStatus.FAILED);
			event.setRetryCount(retries);
			outboxRepository.save(event);
			log.error("Outbox Event {} PERMANENTLY FAILED after {} retries", event.getId(), retries);
		} else {
			event.setRetryCount(retries);
			event.setStatus(OutboxStatus.PENDING);
			outboxRepository.save(event);
			log.warn("Retry {}/{} for Outbox Event {} due to error: {}", retries, MAX_RETRY, event.getId(), error);
		}
	}
}