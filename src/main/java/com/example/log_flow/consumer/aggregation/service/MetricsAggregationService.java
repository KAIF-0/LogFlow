package com.example.log_flow.consumer.aggregation.service;

import com.example.log_flow.consumer.common.service.IngestionEventService;
import com.example.log_flow.ingestion.dto.LifecycleLogRequest;
import com.example.log_flow.ingestion.dto.ValidatedLogBatchMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class MetricsAggregationService {

    private static final int MAX_RETRIES = 3;

    private final StringRedisTemplate redisTemplate;
    private final IngestionEventService ingestionEventService;

    public MetricsAggregationService(StringRedisTemplate redisTemplate,
                                     IngestionEventService ingestionEventService) {
        this.redisTemplate = redisTemplate;
        this.ingestionEventService = ingestionEventService;
    }

    public void aggregate(ValidatedLogBatchMessage message) {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                for (LifecycleLogRequest log : message.getLogs()) {
                    updateMetrics(message.getProjectId(), log);
                }
                return;
            } catch (Exception e) {
                attempts++;
                if (attempts >= MAX_RETRIES) {
                    ingestionEventService.recordFailure(
                            message.getProjectId(),
                            "AGGREGATION_FAILURE",
                            "log.aggregation.queue",
                            message.getLogs().size(),
                            attempts,
                            e.getMessage()
                    );
                }
            }
        }
    }

    private void updateMetrics(Long projectId, LifecycleLogRequest log) {
        String baseKey = "metrics:project:" + projectId;
        String statusKey = baseKey + ":status";
        String endpointKey = baseKey + ":endpoints";

        redisTemplate.opsForHash().increment(baseKey, "total", 1);
        if (log.getStatusCode() >= 500) {
            redisTemplate.opsForHash().increment(baseKey, "failure", 1);
        } else {
            redisTemplate.opsForHash().increment(baseKey, "success", 1);
        }
        redisTemplate.opsForHash().increment(baseKey, "latency_sum", log.getLatencyMs());
        redisTemplate.opsForHash().increment(statusKey, String.valueOf(log.getStatusCode()), 1);
        redisTemplate.opsForHash().increment(endpointKey, log.getPath(), 1);
    }
}