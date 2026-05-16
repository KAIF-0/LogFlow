package com.example.log_flow.consumer.alert.listener;

import com.example.log_flow.consumer.alert.service.AlertService;
import com.example.log_flow.ingestion.dto.ValidatedLogBatchMessage;
import com.example.log_flow.messaging.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AlertListener {

    private final AlertService alertService;

    public AlertListener(AlertService alertService) {
        this.alertService = alertService;
    }

    @RabbitListener(queues = RabbitMqConfig.ALERT_QUEUE)
    public void handle(ValidatedLogBatchMessage message) {
        alertService.process(message);
    }
}