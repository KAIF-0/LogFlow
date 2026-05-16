package com.example.log_flow.messaging.producer;

import com.example.log_flow.ingestion.dto.ValidatedLogBatchMessage;
import com.example.log_flow.messaging.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class LogProducer {

    private final RabbitTemplate rabbitTemplate;

    public LogProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishIngestionBatch(ValidatedLogBatchMessage message) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.INGESTION_EXCHANGE, "", message);
    }

    public void publishProcessedBatch(ValidatedLogBatchMessage message) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.PROCESSING_EXCHANGE, "", message);
    }
}