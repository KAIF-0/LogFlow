package com.example.log_flow.consumer.service_metrics.service;

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

import com.example.log_flow.consumer.common.service.IngestionEventService;
import com.example.log_flow.consumer.service_metrics.entity.ServiceMetrics;
import com.example.log_flow.consumer.service_metrics.repository.ServiceMetricsRepository;
import com.example.log_flow.ingestion.dto.LifecycleLogRequest;
import com.example.log_flow.ingestion.dto.ValidatedLogBatchMessage;
import com.example.log_flow.project.entity.ProjectServiceConfig;
import com.example.log_flow.project.service.ProjectServiceManager;
import com.example.log_flow.project.service.ProjectServiceMatcher;

@Service
public class ServiceMetricsAggregationService {

    private static final int MAX_RETRIES = 3;

    private final StringRedisTemplate redisTemplate;
    private final IngestionEventService ingestionEventService;
    private final ServiceMetricsRepository metricsRepository;
    private final ProjectServiceManager projectServiceManager;
    private final ProjectServiceMatcher projectServiceMatcher;
    private final ObjectMapper objectMapper;

    public ServiceMetricsAggregationService(StringRedisTemplate redisTemplate,
                                            IngestionEventService ingestionEventService,
                                            ServiceMetricsRepository metricsRepository,
                                            ProjectServiceManager projectServiceManager,
                                            ProjectServiceMatcher projectServiceMatcher,
                                            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.ingestionEventService = ingestionEventService;
        this.metricsRepository = metricsRepository;
        this.projectServiceManager = projectServiceManager;
        this.projectServiceMatcher = projectServiceMatcher;
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
                            "SERVICE_METRICS_FAILURE",
                            "log.service-metrics.queue",
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

        List<ProjectServiceConfig> services = projectServiceManager.getServicesForProject(message.getProjectId());
        if (services.isEmpty()) {
            return;
        }

        Map<Long, AggregationDelta> deltas = computeDeltas(services, message.getLogs());
        if (deltas.isEmpty()) {
            return;
        }

        for (Map.Entry<Long, AggregationDelta> entry : deltas.entrySet()) {
            Long serviceId = entry.getKey();
            AggregationDelta delta = entry.getValue();
            ServiceMetrics metrics = metricsRepository.findByProjectIdAndServiceId(message.getProjectId(), serviceId)
                    .orElseGet(() -> {
                        ServiceMetrics created = new ServiceMetrics();
                        created.setProjectId(message.getProjectId());
                        created.setServiceId(serviceId);
                        return created;
                    });

            Map<Integer, Long> statusMap = readStatusCounts(metrics.getStatusCountsJson());
            Map<String, Long> endpointMap = readEndpointCounts(metrics.getEndpointCountsJson());

            metrics.setTotal(metrics.getTotal() + delta.total);
            metrics.setSuccess(metrics.getSuccess() + delta.success);
            metrics.setFailure(metrics.getFailure() + delta.failure);
            metrics.setLatencySum(metrics.getLatencySum() + delta.latencySum);
            metrics.setUpdatedAt(Instant.now());

            for (Map.Entry<Integer, Long> statusEntry : delta.statusCounts.entrySet()) {
                Integer statusCode = statusEntry.getKey();
                Long increment = statusEntry.getValue();
                statusMap.put(statusCode, statusMap.getOrDefault(statusCode, 0L) + increment);
            }

            for (Map.Entry<String, Long> endpointEntry : delta.endpointCounts.entrySet()) {
                String path = endpointEntry.getKey();
                Long increment = endpointEntry.getValue();
                endpointMap.put(path, endpointMap.getOrDefault(path, 0L) + increment);
            }

            metrics.setStatusCountsJson(writeStatusCounts(statusMap));
            metrics.setEndpointCountsJson(writeEndpointCounts(endpointMap));

            metricsRepository.save(metrics);

            String baseKey = "metrics:project:" + message.getProjectId() + ":service:" + serviceId;
            String statusKey = baseKey + ":status";
            String endpointKey = baseKey + ":endpoints";
            redisTemplate.delete(baseKey);
            redisTemplate.delete(statusKey);
            redisTemplate.delete(endpointKey);
            writeCacheSnapshot(baseKey, statusKey, endpointKey, metrics, statusMap, endpointMap);
        }
    }

    private Map<Long, AggregationDelta> computeDeltas(List<ProjectServiceConfig> services, List<LifecycleLogRequest> logs) {
        Map<Long, AggregationDelta> deltas = new HashMap<>();
        for (LifecycleLogRequest log : logs) {
            ProjectServiceConfig service = projectServiceMatcher.resolveService(services, log.getPath());
            if (service == null) {
                continue;
            }
            AggregationDelta delta = deltas.computeIfAbsent(service.getId(), key -> new AggregationDelta());
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
        return deltas;
    }

    private void writeCacheSnapshot(String baseKey,
                                    String statusKey,
                                    String endpointKey,
                                    ServiceMetrics metrics,
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
