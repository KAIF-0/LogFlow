package com.example.log_flow.rules.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RulesResponse {

    private Long projectId;
    private Integer maxPayloadSizeKb;
    private List<String> blockedRoutes;
    private List<String> maskedFields;
    private List<String> headerWhitelist;
    private Integer samplingRate;
    private Integer rateLimitPerMin;
    private Integer alertFailureThreshold;
    private Integer alertTimeWindowSec;
    private Integer alertLatencyThresholdMs;
    private Integer alertLatencyBreachCount;
    private Integer alertLatencyWindowSec;
    private Instant createdAt;
    private Instant updatedAt;
}