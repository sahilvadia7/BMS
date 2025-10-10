//package com.bms.account.config;
//
//import org.springframework.amqp.core.*;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RabbitMQConfig {
//
//    @Value("${rabbitmq.exchange.account}")
//    private String accountExchange;
//
//    @Value("${rabbitmq.queue.accountCreated}")
//    private String accountCreatedQueue;
//
//    @Value("${rabbitmq.routingKey.accountCreated}")
//    private String accountCreatedRoutingKey;
//
//    @Value("${rabbitmq.queue.balanceUpdated}")
//    private String balanceUpdatedQueue;
//
//    @Value("${rabbitmq.routingKey.balanceUpdated}")
//    private String balanceUpdatedRoutingKey;
//
//    // Queues
//    @Bean
//    public Queue accountCreatedQueue() {
//        return new Queue(accountCreatedQueue, true);
//    }
//
//    @Bean
//    public Queue balanceUpdatedQueue() {
//        return new Queue(balanceUpdatedQueue, true);
//    }
//
//    // Exchange
//    @Bean
//    public TopicExchange accountExchange() {
//        return new TopicExchange(accountExchange);
//    }
//
//    // Bindings
//    @Bean
//    public Binding accountCreatedBinding(Queue accountCreatedQueue, TopicExchange accountExchange) {
//        return BindingBuilder.bind(accountCreatedQueue).to(accountExchange).with(accountCreatedRoutingKey);
//    }
//
//    @Bean
//    public Binding balanceUpdatedBinding(Queue balanceUpdatedQueue, TopicExchange accountExchange) {
//        return BindingBuilder.bind(balanceUpdatedQueue).to(accountExchange).with(balanceUpdatedRoutingKey);
//    }
//}
