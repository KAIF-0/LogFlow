package com.example.log_flow.consumer.aggregation.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.log_flow.consumer.aggregation.entity.ProjectMetrics;
import com.example.log_flow.consumer.aggregation.repository.ProjectMetricsRepository;
import com.example.log_flow.consumer.common.service.IngestionEventService;
import com.example.log_flow.ingestion.dto.LifecycleLogRequest;
import com.example.log_flow.ingestion.dto.ValidatedLogBatchMessage;

@Service
public class MetricsAggregationService {

    private static final int MAX_RETRIES = 3;

    private final StringRedisTemplate redisTemplate;
    private final IngestionEventService ingestionEventService;
    private final ProjectMetricsRepository metricsRepository;
    private final ObjectMapper objectMapper;

    public MetricsAggregationService(StringRedisTemplate redisTemplate,
                                     IngestionEventService ingestionEventService,
                                     ProjectMetricsRepository metricsRepository,
                                     ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.ingestionEventService = ingestionEventService;
        this.metricsRepository = metricsRepository;
        this.objectMapper = objectMapper;
    }

    public void aggregate(ValidatedLogBatchMessage message) {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                doAggregate(message);
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

    @Transactional
    protected void doAggregate(ValidatedLogBatchMessage message) {
        if (message.getLogs() == null || message.getLogs().isEmpty()) {
            return;
        }

        AggregationDelta delta = computeDelta(message.getLogs());
        if (delta.total == 0L) {
            return;
        }

        Long projectId = message.getProjectId();
        String baseKey = "metrics:project:" + projectId;
        String statusKey = baseKey + ":status";
        String endpointKey = baseKey + ":endpoints";
        boolean cachePresent = Boolean.TRUE.equals(redisTemplate.hasKey(baseKey));

        ProjectMetrics metrics = metricsRepository.findById(projectId).orElseGet(() -> {
            ProjectMetrics created = new ProjectMetrics();
            created.setProjectId(projectId);
            return created;
        });

        Map<Integer, Long> statusMap = readStatusCounts(metrics.getStatusCountsJson());
        Map<String, Long> endpointMap = readEndpointCounts(metrics.getEndpointCountsJson());

        metrics.setTotal(metrics.getTotal() + delta.total);
        metrics.setSuccess(metrics.getSuccess() + delta.success);
        metrics.setFailure(metrics.getFailure() + delta.failure);
        metrics.setLatencySum(metrics.getLatencySum() + delta.latencySum);
        metrics.setUpdatedAt(Instant.now());

        for (Map.Entry<Integer, Long> entry : delta.statusCounts.entrySet()) {
            Integer statusCode = entry.getKey();
            Long increment = entry.getValue();
            statusMap.put(statusCode, statusMap.getOrDefault(statusCode, 0L) + increment);
        }

        for (Map.Entry<String, Long> entry : delta.endpointCounts.entrySet()) {
            String path = entry.getKey();
            Long increment = entry.getValue();
            endpointMap.put(path, endpointMap.getOrDefault(path, 0L) + increment);
        }

        metrics.setStatusCountsJson(writeStatusCounts(statusMap));
        metrics.setEndpointCountsJson(writeEndpointCounts(endpointMap));

        metricsRepository.save(metrics);

        if (cachePresent) {
            redisTemplate.delete(baseKey);
            redisTemplate.delete(statusKey);
            redisTemplate.delete(endpointKey);
        }
        writeCacheSnapshot(baseKey, statusKey, endpointKey, metrics, statusMap, endpointMap);
    }

    private AggregationDelta computeDelta(List<LifecycleLogRequest> logs) {
        AggregationDelta delta = new AggregationDelta();
        for (LifecycleLogRequest log : logs) {
            delta.total += 1;
            delta.latencySum += log.getLatencyMs();
            if (log.getStatusCode() >= 500) {
                delta.failure += 1;
            } else {
                delta.success += 1;
            }

            Integer statusCode = log.getStatusCode();
            delta.statusCounts.put(statusCode, delta.statusCounts.getOrDefault(statusCode, 0L) + 1);

            String path = log.getPath();
            delta.endpointCounts.put(path, delta.endpointCounts.getOrDefault(path, 0L) + 1);
        }
        return delta;
    }

    private void writeCacheSnapshot(String baseKey,
                                    String statusKey,
                                    String endpointKey,
                                    ProjectMetrics metrics,
                                    Map<Integer, Long> statusMap,
                                    Map<String, Long> endpointMap) {
        Map<String, String> baseValues = new HashMap<>();
        baseValues.put("total", String.valueOf(metrics.getTotal()));
        baseValues.put("success", String.valueOf(metrics.getSuccess()));
        baseValues.put("failure", String.valueOf(metrics.getFailure()));
        baseValues.put("latency_sum", String.valueOf(metrics.getLatencySum()));
        redisTemplate.opsForHash().putAll(baseKey, baseValues);

        if (!statusMap.isEmpty()) {
            Map<String, String> statusValues = new HashMap<>();
            for (Map.Entry<Integer, Long> entry : statusMap.entrySet()) {
                statusValues.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
            redisTemplate.opsForHash().putAll(statusKey, statusValues);
        }

        if (!endpointMap.isEmpty()) {
            Map<String, String> endpointValues = new HashMap<>();
            for (Map.Entry<String, Long> entry : endpointMap.entrySet()) {
                endpointValues.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
            redisTemplate.opsForHash().putAll(endpointKey, endpointValues);
        }
    }

    private Map<Integer, Long> readStatusCounts(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<Integer, Long>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    private Map<String, Long> readEndpointCounts(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Long>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    private String writeStatusCounts(Map<Integer, Long> statusMap) {
        try {
            return objectMapper.writeValueAsString(statusMap);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String writeEndpointCounts(Map<String, Long> endpointMap) {
        try {
            return objectMapper.writeValueAsString(endpointMap);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private static class AggregationDelta {
        private long total = 0L;
        private long success = 0L;
        private long failure = 0L;
        private long latencySum = 0L;
        private final Map<Integer, Long> statusCounts = new HashMap<>();
        private final Map<String, Long> endpointCounts = new HashMap<>();
    }
}