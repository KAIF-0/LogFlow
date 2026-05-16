package com.example.log_flow.consumer.rules.listener;

import com.example.log_flow.consumer.rules.service.RulesProcessingService;
import com.example.log_flow.ingestion.dto.ValidatedLogBatchMessage;
import com.example.log_flow.messaging.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RulesListener {

    private final RulesProcessingService rulesProcessingService;

    public RulesListener(RulesProcessingService rulesProcessingService) {
        this.rulesProcessingService = rulesProcessingService;
    }

    @RabbitListener(queues = RabbitMqConfig.INGESTION_QUEUE)
    public void handle(ValidatedLogBatchMessage message) {
        rulesProcessingService.process(message);
    }
}