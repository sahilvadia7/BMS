package com.bms.ledger.consumer;

import com.bms.ledger.config.RabbitMQConfig;
import com.bms.ledger.enums.LedgerType;
import com.bms.ledger.service.LedgerService;
import com.bms.ledger.producer.LedgerResultProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.math.BigDecimal;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionEventConsumer {

	private final LedgerService ledgerService;
	private final LedgerResultProducer resultProducer;

	@RabbitListener(queues = RabbitMQConfig.TX_TO_LEDGER_QUEUE)
	public void consumeTransactionEvent(HashMap<String, Object> map) {

		log.info("Received event: {}", map);

		String eventType = (String) map.get("eventType");
		if (eventType == null) {
			log.warn("Unknown event type in message: {}", map);
			return;
		}

		try {
			switch (eventType) {

				case "DEBIT_REQUESTED":
					handleStage(map, LedgerType.DEBIT_REQUESTED);
					break;
				case "DEBIT_SUCCESS":
					handleStage(map, LedgerType.DEBIT_SUCCESS);
					break;
				case "DEBIT_FAILED":
					handleStage(map, LedgerType.DEBIT_FAILED);
					break;
				case "CREDIT_REQUESTED":
					handleStage(map, LedgerType.CREDIT_REQUESTED);
					break;
				case "CREDIT_SUCCESS":
					handleStage(map, LedgerType.CREDIT_SUCCESS);
					break;
				case "CREDIT_FAILED":
					handleStageFailure(map, LedgerType.CREDIT_FAILED);
					break;
				case "COMPENSATION_DEBIT":
					handleStage(map, LedgerType.COMPENSATION_DEBIT);
					break;
				case "COMPENSATION_CREDIT":
					handleStage(map, LedgerType.COMPENSATION_CREDIT);
					break;
				case "COMPLETED":
					ledgerService.processLedgerEntry(
							(String) map.get("transactionId"),
							null,
							map.get("amount") != null ? new BigDecimal(map.get("amount").toString()) : BigDecimal.ZERO,
							LedgerType.COMPLETED,
							"COMPLETED",
							(String) map.get("message"),
							true,
							null
					);
					publishFinalSuccess(map);
					break;
				case "FAILED":
					ledgerService.processLedgerEntry(
							(String) map.get("transactionId"),
							null,
							BigDecimal.ZERO,
							LedgerType.FAILED,
							"FAILED",
							(String) map.get("message"),
							false,
							(String) map.get("failureReason")
					);
					publishFinalFailure(
							(String) map.get("transactionId"),
							(String) map.get("failureReason"),
							(String) map.get("message")
					);
					break;
				default:
					log.warn("Unrecognized event type {}", eventType);
			}
		} catch (Exception e) {
			log.error("Ledger processing exception for txn={}", map.get("transactionId"), e);
			publishFinalFailure(
					(String) map.get("transactionId"),
					"Ledger crashed: " + e.getMessage(),
					(String) map.get("message")
			);
		}
	}

	private void handleStage(HashMap<String, Object> map, LedgerType type) {
		boolean success = map.get("success") == null || (boolean) map.get("success");
		String failureReason = (String) map.get("failureReason");

		ledgerService.processLedgerEntry(
				(String) map.get("transactionId"),
				(String) map.get("accountNumber"),
				new BigDecimal(map.get("amount").toString()),
				type,
				type.name(),
				(String) map.get("description"),
				success,
				failureReason
		);

		publishStageAck(
				(String) map.get("transactionId"),
				type,
				success,
				failureReason
		);
	}

	private void handleStageFailure(HashMap<String, Object> map, LedgerType type) {
		handleStage(map, type);
		publishFinalFailure(
				(String) map.get("transactionId"),
				(String) map.get("failureReason"),
				(String) map.get("message")
		);
	}

	private void publishStageAck(String txnId, LedgerType stage, boolean success, String failureReason) {
		HashMap<String, Object> result = new HashMap<>();
		result.put("eventType", "LEDGER_STAGE_ACK");
		result.put("transactionId", txnId);
		result.put("stage", stage.name());
		result.put("success", success);
		result.put("failureReason", failureReason);

		resultProducer.publishLedgerResult(result);
	}

	private void publishFinalSuccess(HashMap<String, Object> map) {
		HashMap<String, Object> result = new HashMap<>();
		result.put("eventType", "TRANSACTION_SUCCESS");
		result.put("transactionId", map.get("transactionId"));
		result.put("amount", map.get("amount"));
		result.put("success", true);
		result.put("message", map.get("message"));

		resultProducer.publishLedgerResult(result);
	}

	private void publishFinalFailure(String txnId, String failureReason, String message) {
		HashMap<String, Object> result = new HashMap<>();
		result.put("eventType", "TRANSACTION_FAILURE");
		result.put("transactionId", txnId);
		result.put("success", false);
		result.put("failureReason", failureReason);
		result.put("message", message);

		resultProducer.publishLedgerResult(result);
	}
}
