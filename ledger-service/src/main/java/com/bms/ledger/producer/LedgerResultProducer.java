package com.bms.ledger.producer;

import com.bms.ledger.config.RabbitMQConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class LedgerResultProducer {

	private final RabbitTemplate rabbitTemplate;

	public void publishLedgerResult(HashMap<String, Object> event) {
		log.info("Publishing Ledger Result: {}", event);

		rabbitTemplate.convertAndSend(
				RabbitMQConfig.EXCHANGE,
				RabbitMQConfig.LEDGER_TO_TX_KEY,
				event
		);
	}
}

