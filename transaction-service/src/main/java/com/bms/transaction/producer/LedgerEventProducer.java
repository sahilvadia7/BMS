package com.bms.transaction.producer;

import com.bms.transaction.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class LedgerEventProducer {

	private final RabbitTemplate rabbitTemplate;

	private void sendEvent(HashMap<String, Object> event) {
		rabbitTemplate.convertAndSend(
				RabbitMQConfig.EXCHANGE,
				RabbitMQConfig.TX_TO_LEDGER_KEY,
				event
		);
	}

	private void publishEvent(String eventType, String txnId, String accountNumber,
							  String description, Object amount, boolean success, String failureReason) {

		if (failureReason != null && failureReason.length() > 255) {
			failureReason = failureReason.substring(0, 255);
		}

		HashMap<String, Object> event = new HashMap<>();
		event.put("eventType", eventType);
		event.put("transactionId", txnId);
		event.put("accountNumber", accountNumber);
		event.put("description", description);
		event.put("amount", amount);
		event.put("success", success);
		event.put("failureReason", failureReason);
		event.put("timestamp", System.currentTimeMillis());

		sendEvent(event);
	}

	public void debitRequested(String txnId, String acc, Object amount, String desc) {
		publishEvent("DEBIT_REQUESTED", txnId, acc, desc, amount, false, null);
	}

	public void debitSuccess(String txnId, String acc, Object amount, String desc) {
		publishEvent("DEBIT_SUCCESS", txnId, acc, desc, amount, true, null);
	}

	public void debitFailed(String txnId, String acc, Object amount, String desc, String failureReason) {
		publishEvent("DEBIT_FAILED", txnId, acc, desc, amount, false, failureReason);
	}

	public void creditRequested(String txnId, String acc, Object amount, String desc) {
		publishEvent("CREDIT_REQUESTED", txnId, acc, desc, amount, false, null);
	}

	public void creditSuccess(String txnId, String acc, Object amount, String desc) {
		publishEvent("CREDIT_SUCCESS", txnId, acc, desc, amount, true, null);
	}

	public void creditFailed(String txnId, String acc, Object amount, String desc, String failureReason) {
		publishEvent("CREDIT_FAILED", txnId, acc, desc, amount, false, failureReason);
	}

	public void compensationCredit(String txnId, String acc, Object amount, String desc, boolean success, String failureReason) {
		publishEvent("COMPENSATION_CREDIT", txnId, acc, desc, amount, success, failureReason);
	}

	public void compensationDebit(String txnId, String acc, Object amount, String desc, boolean success, String failureReason) {
		publishEvent("COMPENSATION_DEBIT", txnId, acc, desc, amount, success, failureReason);
	}

	public void transactionCompleted(String txnId, BigDecimal amount, String message) {
		HashMap<String, Object> event = new HashMap<>();
		event.put("eventType", "COMPLETED");
		event.put("transactionId", txnId);
		event.put("amount", amount);
		event.put("success", true);
		event.put("message", message);
		event.put("timestamp", System.currentTimeMillis());

		sendEvent(event);
	}

	public boolean transactionFailed(String txnId, String message, String failureReason) {
		try {
			if (failureReason != null && failureReason.length() > 255) {
				failureReason = failureReason.substring(0, 255);
			}

			HashMap<String, Object> event = new HashMap<>();
			event.put("eventType", "FAILED");
			event.put("transactionId", txnId);
			event.put("success", false);
			event.put("failureReason", failureReason);
			event.put("message", message);
			event.put("timestamp", System.currentTimeMillis());

			sendEvent(event);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
