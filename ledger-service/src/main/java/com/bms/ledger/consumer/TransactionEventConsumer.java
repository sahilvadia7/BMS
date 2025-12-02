package com.bms.ledger.consumer;

import com.bms.ledger.config.RabbitMQConfig;
import com.bms.ledger.enums.LedgerType;
import com.bms.ledger.events.AccountCreditEvent;
import com.bms.ledger.events.AccountDebitEvent;
import com.bms.ledger.events.LedgerEntryEvent;
import com.bms.ledger.events.LedgerResultEvent;
import com.bms.ledger.producer.LedgerResultProducer;
import com.bms.ledger.service.LedgerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

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
			log.warn("Unknown event type: {}", map);
			return;
		}

		switch (eventType) {
			case "DEBIT":
				handleDebit(map);
				break;

			case "CREDIT":
				handleCredit(map);
				break;

			case "LEDGER_ENTRY":
				handleLedgerEntry(map);
				break;

			default:
				log.warn("Unrecognized event type {}", eventType);
		}
	}

	private void handleDebit(HashMap<String, Object> map) {
		log.info("Handling Debit event: {}", map);

		ledgerService.processLedgerEntry(
				(String) map.get("transactionId"),
				(String) map.get("accountNumber"),
				new BigDecimal(map.get("amount").toString()),
				LedgerType.DEBIT,
				(String) map.get("description"),
				"DEBIT"
		);

		publishSuccess((String) map.get("transactionId"));
	}

	private void handleCredit(HashMap<String, Object> map) {
		log.info("Handling Credit event: {}", map);

		ledgerService.processLedgerEntry(
				(String) map.get("transactionId"),
				(String) map.get("accountNumber"),
				new BigDecimal(map.get("amount").toString()),
				LedgerType.CREDIT,
				(String) map.get("description"),
				"CREDIT"
		);

		publishSuccess((String) map.get("transactionId"));
	}

	private void handleLedgerEntry(HashMap<String, Object> map) {
		log.info("Handling Ledger Entry event: {}", map);

		ledgerService.processLedgerEntry(
				(String) map.get("transactionId"),
				(String) map.get("accountNumber"),
				new BigDecimal(map.get("amount").toString()),
				LedgerType.valueOf((String) map.get("ledgerType")),
				(String) map.get("description"),
				"LEDGER_ENTRY"
		);

		publishSuccess((String) map.get("transactionId"));
	}

	private void publishSuccess(String txnId) {
		HashMap<String, Object> result = new HashMap<>();
		result.put("eventType", "LEDGER_RESULT");
		result.put("transactionId", txnId);
		result.put("success", true);
		result.put("message", "Ledger processed successfully");

		resultProducer.publishLedgerResult(result);
	}
}
