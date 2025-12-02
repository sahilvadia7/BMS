package com.bms.transaction.producer;

import com.bms.transaction.config.RabbitMQConfig;
import com.bms.transaction.events.AccountCreditEvent;
import com.bms.transaction.events.AccountDebitEvent;
import com.bms.transaction.events.LedgerEntryEvent;
import com.bms.transaction.events.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class LedgerEventProducer {

	private final RabbitTemplate rabbitTemplate;

	private void send(HashMap<String, Object> event) {
		rabbitTemplate.convertAndSend(
				RabbitMQConfig.EXCHANGE,
				RabbitMQConfig.TX_TO_LEDGER_KEY,
				event
		);
	}

	public void publishLedgerEntry(HashMap<String, Object> event) {
		send(event);
	}

	public void publishAccountDebit(HashMap<String, Object> event) {
		event.put("eventType", "DEBIT");
		send(event);
	}

	public void publishAccountCredit(HashMap<String, Object> event) {
		event.put("eventType", "CREDIT");
		send(event);
	}
}
