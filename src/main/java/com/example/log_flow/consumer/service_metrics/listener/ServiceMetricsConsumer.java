package com.example.log_flow.consumer.service_metrics.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.log_flow.consumer.service_metrics.service.ServiceMetricsAggregationService;
import com.example.log_flow.ingestion.dto.ValidatedLogBatchMessage;
import com.example.log_flow.messaging.config.RabbitMqConfig;

@Component
public class ServiceMetricsConsumer {

    private final ServiceMetricsAggregationService serviceMetricsAggregationService;

    public ServiceMetricsConsumer(ServiceMetricsAggregationService serviceMetricsAggregationService) {
        this.serviceMetricsAggregationService = serviceMetricsAggregationService;
    }

    @RabbitListener(queues = RabbitMqConfig.SERVICE_METRICS_QUEUE)
    public void handle(ValidatedLogBatchMessage message) {
        serviceMetricsAggregationService.aggregate(message);
    }
}
