package com.example.log_flow.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String INGESTION_EXCHANGE = "log.ingestion.exchange";
    public static final String INGESTION_QUEUE = "log.ingestion.processing.queue";

    public static final String PROCESSING_EXCHANGE = "log.processing.exchange";
    public static final String PERSISTENCE_QUEUE = "log.persistence.queue";
    public static final String AGGREGATION_QUEUE = "log.aggregation.queue";
    public static final String ALERT_QUEUE = "log.alert.queue";

    @Bean
    public Exchange ingestionExchange() {
        return new DirectExchange(INGESTION_EXCHANGE);
    }

    @Bean
    public Queue ingestionQueue() {
        return QueueBuilder.durable(INGESTION_QUEUE).build();
    }

    @Bean
    public Binding ingestionBinding(Exchange ingestionExchange, Queue ingestionQueue) {
        return BindingBuilder.bind(ingestionQueue).to(ingestionExchange).with("").noargs();
    }

    @Bean
    public Exchange processingExchange() {
        return new FanoutExchange(PROCESSING_EXCHANGE);
    }

    @Bean
    public Queue persistenceQueue() {
        return QueueBuilder.durable(PERSISTENCE_QUEUE).build();
    }

    @Bean
    public Queue aggregationQueue() {
        return QueueBuilder.durable(AGGREGATION_QUEUE).build();
    }

    @Bean
    public Queue alertQueue() {
        return QueueBuilder.durable(ALERT_QUEUE).build();
    }

    @Bean
    public Binding persistenceBinding(Exchange processingExchange, Queue persistenceQueue) {
        return BindingBuilder.bind(persistenceQueue).to(processingExchange).with("").noargs();
    }

    @Bean
    public Binding aggregationBinding(Exchange processingExchange, Queue aggregationQueue) {
        return BindingBuilder.bind(aggregationQueue).to(processingExchange).with("").noargs();
    }

    @Bean
    public Binding alertBinding(Exchange processingExchange, Queue alertQueue) {
        return BindingBuilder.bind(alertQueue).to(processingExchange).with("").noargs();
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        template.setBeforePublishPostProcessors(message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });
        return template;
    }
}