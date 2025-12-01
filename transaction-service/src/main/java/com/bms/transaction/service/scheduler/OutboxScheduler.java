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
public class OutboxScheduler {

	private final OutboxRepository outboxRepository;
	private final TransactionRepository transactionRepository;
	private final PaymentClient paymentClient;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private static final int MAX_RETRY = 5;

	@Scheduled(fixedDelay = 3000)
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

				boolean permanentlyFailed = handleFailure(event, e.getMessage());

				if (permanentlyFailed) {
					failTransaction(event, e.getMessage());
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
			log.info("External Response : " + response.toString());
		} catch (Exception e) {
			throw new RuntimeException("Payment Gateway Call Failed: " + e.getMessage());
		}

		Transaction txn = transactionRepository.findByTransactionId(event.getAggregateId())
				.orElseThrow(() -> new RuntimeException("Transaction not found"));

		if (response.getStatus() == TransactionStatus.COMPLETED) {

			txn.setStatus(TransactionStatus.COMPLETED);
			txn.setExternalReferenceId(response.getExternalReferenceId());
			txn.setGatewayAcknowledgedAt(LocalDateTime.now());
			txn.setCompletedAt(LocalDateTime.now());
			txn.setLinkedTransactionId(response.getExternalReferenceId());
			transactionRepository.save(txn);

			event.setStatus(OutboxStatus.SENT);
			outboxRepository.save(event);

			log.info("External Payment SUCCESS → txnId={} marked COMPLETED", txn.getTransactionId());

		} else {
			boolean permanentlyFailed = handleFailure(event, response.getFailureReason());

			txn.setStatus(TransactionStatus.FAILED);
			txn.setFailureReason(response.getFailureReason());
			txn.setCompletedAt(LocalDateTime.now());
			transactionRepository.save(txn);

			log.warn("External Payment FAILED → txnId={} marked FAILED", txn.getTransactionId());

			if (permanentlyFailed) {
				log.error("Transaction {} permanently failed after {} retries",
						txn.getTransactionId());
			}
		}
	}

	private PaymentRequest parsePayload(String payloadJson) {
		try {
			log.info("Parsing Payload: {}", payloadJson);
			return objectMapper.readValue(payloadJson, PaymentRequest.class);

		} catch (Exception e) {
			log.error("Payload parsing failed! Raw Payload = {}", payloadJson);
			throw new RuntimeException("Failed to parse Outbox Payload: " + e.getMessage());
		}
	}

	private boolean handleFailure(OutboxEvent event, String error) {

		int retries = event.getRetryCount() + 1;
		event.setRetryCount(retries);

		if (retries >= MAX_RETRY) {
			event.setStatus(OutboxStatus.FAILED);
			outboxRepository.save(event);
			return true;
		}

		event.setStatus(OutboxStatus.PENDING);
		outboxRepository.save(event);
		return false;
	}

	private void failTransaction(OutboxEvent event, String error) {

		Transaction txn = transactionRepository.findByTransactionId(event.getAggregateId())
				.orElseThrow(() -> new RuntimeException("Transaction not found"));

		txn.setStatus(TransactionStatus.FAILED);
		txn.setFailureReason(error);
		txn.setCompletedAt(LocalDateTime.now());

		transactionRepository.save(txn);
	}
}
