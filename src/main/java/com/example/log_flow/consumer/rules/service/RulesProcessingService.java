package com.example.log_flow.consumer.rules.service;

import com.example.log_flow.common.exception.AppException;
import com.example.log_flow.ingestion.dto.LifecycleLogRequest;
import com.example.log_flow.ingestion.dto.ValidatedLogBatchMessage;
import com.example.log_flow.messaging.producer.LogProducer;
import com.example.log_flow.rules.entity.ProjectRules;
import com.example.log_flow.rules.repository.ProjectRulesRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import com.example.log_flow.rules.service.BlockedRouteMatcher;

@Service
public class RulesProcessingService {

    private final ProjectRulesRepository rulesRepository;
    private final LogProducer logProducer;
    private final ObjectMapper objectMapper;

    private final Map<Long, Bucket> projectBuckets = new ConcurrentHashMap<>();
    private final Map<Long, Integer> projectBucketRates = new ConcurrentHashMap<>();

    public RulesProcessingService(ProjectRulesRepository rulesRepository,
                                  LogProducer logProducer,
                                  ObjectMapper objectMapper) {
        this.rulesRepository = rulesRepository;
        this.logProducer = logProducer;
        this.objectMapper = objectMapper;
    }

    public void process(ValidatedLogBatchMessage message) {
        ProjectRules rules = rulesRepository.findByProjectId(message.getProjectId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "rules_not_found", "Rules not found"));

        List<String> blockedRoutes = readList(rules.getBlockedRoutesJson());
        Set<String> maskedSet = Set.copyOf(readList(rules.getMaskedFieldsJson()));
        Set<String> headerWhitelist = Set.copyOf(readList(rules.getHeaderWhitelistJson()));

        int samplingRate = rules.getSamplingRate();
        int maxPayloadBytes = rules.getMaxPayloadSizeKb() * 1024;

        Bucket projectBucket = getBucket(message.getProjectId(), rules.getRateLimitPerMin());

        List<LifecycleLogRequest> validLogs = new ArrayList<>();
        for (LifecycleLogRequest log : message.getLogs()) {
            if (!projectBucket.tryConsume(1)) {
                continue;
            }

            if (samplingRate < 100 && samplingRate > 0) {
                int sample = ThreadLocalRandom.current().nextInt(1, 101);
                if (sample > samplingRate) {
                    continue;
                }
            }

            if (samplingRate == 0) {
                continue;
            }

            if (BlockedRouteMatcher.isBlocked(blockedRoutes, log.getPath())) {
                continue;
            }

            int payloadSize = sizeOf(log.getRequestBody()) + sizeOf(log.getResponseBody());
            if (payloadSize > maxPayloadBytes) {
                continue;
            }

            if (!isValidJson(log.getRequestBody()) || !isValidJson(log.getResponseBody())) {
                continue;
            }

            if (!headerWhitelist.isEmpty()) {
                log.setRequestHeaders(filterHeaders(log.getRequestHeaders(), headerWhitelist));
                log.setResponseHeaders(filterHeaders(log.getResponseHeaders(), headerWhitelist));
            }

            if (!maskedSet.isEmpty()) {
                log.setRequestBody(maskBody(log.getRequestBody(), maskedSet));
                log.setResponseBody(maskBody(log.getResponseBody(), maskedSet));
            }

            validLogs.add(log);
        }

        if (validLogs.isEmpty()) {
            return;
        }

        ValidatedLogBatchMessage cleaned = new ValidatedLogBatchMessage(
                message.getProjectId(),
                Instant.now(),
                validLogs
        );
        logProducer.publishProcessedBatch(cleaned);
    }

    private Map<String, String> filterHeaders(Map<String, String> headers, Set<String> whitelist) {
        if (headers == null || headers.isEmpty()) return headers;
        Map<String, String> filtered = new java.util.HashMap<>();
        headers.forEach((k, v) -> {
            if (whitelist.contains(k)) {
                filtered.put(k, v);
            }
        });
        return filtered;
    }

    private String maskBody(String body, Set<String> maskedFields) {
        if (body == null || body.isBlank()) return body;
        try {
            Map<String, Object> json = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
            maskMap(json, normalizeMaskedFields(maskedFields));
            return objectMapper.writeValueAsString(json);
        } catch (Exception e) {
            return body;
        }
    }

    private void maskMap(Map<String, Object> map, Set<String> maskedFields) {
        map.forEach((key, value) -> {
            if (key != null && maskedFields.contains(key.toLowerCase())) {
                map.put(key, "***");
            } else if (value instanceof Map<?, ?> nested) {
                maskMap((Map<String, Object>) nested, maskedFields);
            }
        });
    }

    private Set<String> normalizeMaskedFields(Set<String> maskedFields) {
        if (maskedFields == null || maskedFields.isEmpty()) {
            return Set.of();
        }
        return maskedFields.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.toLowerCase())
                .collect(java.util.stream.Collectors.toSet());
    }

    private boolean isValidJson(String body) {
        if (body == null || body.isBlank()) return true;
        try {
            objectMapper.readTree(body);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private int sizeOf(String body) {
        if (body == null) return 0;
        return body.getBytes(StandardCharsets.UTF_8).length;
    }

    private List<String> readList(String value) {
        if (value == null || value.isBlank()) return List.of();
        try {
            return objectMapper.readValue(value, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private Bucket getBucket(Long projectId, int rateLimitPerMin) {
        Integer current = projectBucketRates.get(projectId);
        if (current == null || current != rateLimitPerMin) {
            projectBucketRates.put(projectId, rateLimitPerMin);
            Bandwidth limit = Bandwidth.classic(rateLimitPerMin, Refill.greedy(rateLimitPerMin, Duration.ofMinutes(1)));
            Bucket bucket = Bucket.builder().addLimit(limit).build();
            projectBuckets.put(projectId, bucket);
            return bucket;
        }
        return projectBuckets.get(projectId);
    }
}