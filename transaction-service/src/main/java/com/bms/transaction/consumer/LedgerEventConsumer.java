package com.bms.transaction.consumer;

import com.bms.transaction.config.RabbitMQConfig;
import com.bms.transaction.enums.TransactionStatus;
import com.bms.transaction.model.Transaction;
import com.bms.transaction.producer.LedgerEventProducer;
import com.bms.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerEventConsumer {

	private final TransactionRepository transactionRepository;
	private final LedgerEventProducer ledgerEventProducer;

	@SuppressWarnings("unchecked")
	@RabbitListener(queues = RabbitMQConfig.LEDGER_TO_TX_QUEUE)
	public void consumeLedgerResult(Map<String, Object> map) {

		if (map == null || map.get("transactionId") == null) {
			log.error("Received Ledger Event with null transactionId: {}", map);
			return;
		}

		String txnId = map.get("transactionId").toString();
		String eventType = map.getOrDefault("eventType", "").toString();
		String stage = map.getOrDefault("stage", "").toString();
		boolean success = map.get("success") != null && Boolean.parseBoolean(map.get("success").toString());
		String failureReason = map.get("failureReason") != null ? map.get("failureReason").toString() : null;

		log.info("Ledger Result: eventType={}, stage={}, success={}, txn={}",
				eventType, stage, success, txnId);

		Transaction txn = transactionRepository.findByTransactionId(txnId).orElse(null);
		if (txn == null) {
			log.error("Transaction not found: {}", txnId);
			return;
		}

		if ("TRANSACTION_SUCCESS".equalsIgnoreCase(eventType)) {
			txn.setStatus(TransactionStatus.COMPLETED);
			transactionRepository.save(txn);
		} else if ("TRANSACTION_FAILED".equalsIgnoreCase(eventType)) {
			txn.setStatus(TransactionStatus.FAILED);
			txn.setFailureReason(failureReason);
			transactionRepository.save(txn);
			return;
		}

		if (stage.isEmpty()) {
			log.warn("Ledger event stage is empty for txn {}", txnId);
			return;
		}

		switch (stage.toUpperCase()) {

			case "DEBIT_REQUESTED":
				txn.setStatus(TransactionStatus.DEBIT_PENDING);
				transactionRepository.save(txn);
				break;

			case "CREDIT_REQUESTED":
				txn.setStatus(TransactionStatus.CREDIT_PENDING);
				transactionRepository.save(txn);
				break;

			case "DEBIT_SUCCESS":
				txn.setStatus(TransactionStatus.DEBIT_COMPLETED);
				transactionRepository.save(txn);

				if ("TRANSFER".equalsIgnoreCase(txn.getTransactionType().name())
						&& txn.getDestinationAccountNumber() != null) {

					ledgerEventProducer.creditRequested(
							txnId,
							txn.getDestinationAccountNumber(),
							txn.getAmount(),
							"Credit after debit success"
					);
				}
				break;

			case "DEBIT_FAILED":
				txn.setStatus(TransactionStatus.FAILED);
				txn.setFailureReason(failureReason);
				transactionRepository.save(txn);
				break;

			case "CREDIT_SUCCESS":
				txn.setStatus(TransactionStatus.COMPLETED);
				transactionRepository.save(txn);
				break;

			case "CREDIT_FAILED":
				txn.setStatus(TransactionStatus.COMPENSATION);
				txn.setFailureReason(failureReason);
				transactionRepository.save(txn);

				ledgerEventProducer.compensationDebit(
						txnId,
						txn.getAccountNumber(),
						txn.getAmount(),
						"Compensation after credit failed",
						true,
						failureReason
				);
				break;

			case "COMPENSATION_DEBIT":
			case "COMPENSATION_CREDIT":
				txn.setStatus(success ?
						TransactionStatus.COMPENSATION_COMPLETED :
						TransactionStatus.COMPENSATION_FAILED
				);
				txn.setFailureReason(failureReason);
				transactionRepository.save(txn);
				break;

			default:
				log.warn("Unhandled Ledger Stage '{}' for txn {}", stage, txnId);
		}
	}
}