package com.example.log_flow.consumer.persistence.listener;

import com.example.log_flow.consumer.persistence.service.PersistenceService;
import com.example.log_flow.ingestion.dto.ValidatedLogBatchMessage;
import com.example.log_flow.messaging.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PersistenceListener {

    private final PersistenceService persistenceService;

    public PersistenceListener(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @RabbitListener(queues = RabbitMqConfig.PERSISTENCE_QUEUE)
    public void handle(ValidatedLogBatchMessage message) {
        persistenceService.persist(message);
    }
}