package com.bms.transaction.consumer;

import com.bms.transaction.config.RabbitMQConfig;
import com.bms.transaction.enums.TransactionStatus;
import com.bms.transaction.events.LedgerResultEvent;
import com.bms.transaction.model.Transaction;
import com.bms.transaction.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerEventConsumer {

	private final TransactionRepository transactionRepository;

	@RabbitListener(queues = RabbitMQConfig.LEDGER_TO_TX_QUEUE)
	public void consumeLedgerResult(HashMap<String, Object> map) {

		log.info("Received Ledger Result: {}", map);

		String txnId = (String) map.get("transactionId");
		boolean success = (boolean) map.get("success");
		String message = (String) map.get("message");

		Transaction txn = transactionRepository.findByTransactionId(txnId)
				.orElse(null);

		if (txn == null) {
			log.error("Transaction not found: {}", txnId);
			return;
		}

		if (success) {
			txn.setStatus(TransactionStatus.COMPLETED);
		} else {
			txn.setStatus(TransactionStatus.FAILED);
		}

		transactionRepository.save(txn);
	}
}
