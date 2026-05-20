package com.example.log_flow.consumer.project_metrics.listener;

import com.example.log_flow.consumer.project_metrics.service.ProjectMetricsAggregationService;
import com.example.log_flow.ingestion.dto.ValidatedLogBatchMessage;
import com.example.log_flow.messaging.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ProjectMetricsConsumer {

    private final ProjectMetricsAggregationService metricsAggregationService;

    public ProjectMetricsConsumer(ProjectMetricsAggregationService metricsAggregationService) {
        this.metricsAggregationService = metricsAggregationService;
    }

    @RabbitListener(queues = RabbitMqConfig.PROJECT_METRICS_QUEUE)
    public void handle(ValidatedLogBatchMessage message) {
        metricsAggregationService.aggregate(message);
    }
}