package com.example.log_flow.consumer.aggregation.listener;

import com.example.log_flow.consumer.aggregation.service.MetricsAggregationService;
import com.example.log_flow.ingestion.dto.ValidatedLogBatchMessage;
import com.example.log_flow.messaging.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MetricsAggregationConsumer {

    private final MetricsAggregationService metricsAggregationService;

    public MetricsAggregationConsumer(MetricsAggregationService metricsAggregationService) {
        this.metricsAggregationService = metricsAggregationService;
    }

    @RabbitListener(queues = RabbitMqConfig.AGGREGATION_QUEUE)
    public void handle(ValidatedLogBatchMessage message) {
        metricsAggregationService.aggregate(message);
    }
}