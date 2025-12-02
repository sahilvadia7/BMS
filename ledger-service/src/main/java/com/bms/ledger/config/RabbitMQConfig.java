package com.bms.ledger.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMQConfig {

	// Exchange
	public static final String EXCHANGE = "transaction.exchange";

	// Queues
	public static final String TX_TO_LEDGER_QUEUE = "transaction.ledger.queue";
	public static final String LEDGER_TO_TX_QUEUE = "ledger.transaction.queue";

	// Routing Keys
	public static final String TX_TO_LEDGER_KEY = "transaction.ledger";
	public static final String LEDGER_TO_TX_KEY = "ledger.transaction";

	@Bean
	public TopicExchange transactionExchange() {
		return new TopicExchange(EXCHANGE, true, false);
	}

	@Bean
	public Queue txToLedgerQueue() {
		return new Queue(TX_TO_LEDGER_QUEUE, true);
	}

	@Bean
	public Queue ledgerToTxQueue() {
		return new Queue(LEDGER_TO_TX_QUEUE, true);
	}

	@Bean
	public Binding bindTxToLedger(Queue txToLedgerQueue, TopicExchange exchange) {
		return BindingBuilder.bind(txToLedgerQueue)
				.to(exchange)
				.with(TX_TO_LEDGER_KEY);
	}

	@Bean
	public Binding bindLedgerToTx(Queue ledgerToTxQueue, TopicExchange exchange) {
		return BindingBuilder.bind(ledgerToTxQueue)
				.to(exchange)
				.with(LEDGER_TO_TX_KEY);
	}

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	@Primary
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(jsonMessageConverter());
		return template;
	}
}
