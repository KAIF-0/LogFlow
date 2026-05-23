package com.example.log_flow.rules.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RulesRequest {

    @NotNull
    @Min(value = 1, message = "Max payload size must be at least 1 KB")
    @Max(value = 10240, message = "Max payload size must be at most 10240 KB")
    private Integer maxPayloadSizeKb;

    @NotNull
    @jakarta.validation.constraints.Size(max = 1000, message = "Blocked routes must be at most 1000")
    private List<@jakarta.validation.constraints.NotBlank String> blockedRoutes;

    @NotNull
    @jakarta.validation.constraints.Size(max = 1000, message = "Masked fields must be at most 1000")
    private List<@jakarta.validation.constraints.NotBlank String> maskedFields;

    @NotNull
    @jakarta.validation.constraints.Size(max = 1000, message = "Header whitelist must be at most 1000")
    private List<@jakarta.validation.constraints.NotBlank String> headerWhitelist;

    @NotNull
    @Min(value = 0, message = "Sampling rate must be at least 0")
    @Max(value = 100, message = "Sampling rate must be at most 100")
    private Integer samplingRate;

    @NotNull
    @Min(value = 1, message = "Rate limit per minute must be at least 1")
    @Max(value = 100000, message = "Rate limit per minute must be at most 100000")
    private Integer rateLimitPerMin;

    @Min(value = 1, message = "Alert failure threshold must be at least 1")
    @Max(value = 100000, message = "Alert failure threshold must be at most 100000")
    private Integer alertFailureThreshold;

    @Min(value = 1, message = "Alert time window must be at least 1 second")
    @Max(value = 86400, message = "Alert time window must be at most 86400 seconds")
    private Integer alertTimeWindowSec;

    @Min(value = 1, message = "Alert latency threshold must be at least 1 ms")
    @Max(value = 60000, message = "Alert latency threshold must be at most 60000 ms")
    private Integer alertLatencyThresholdMs;

    @Min(value = 1, message = "Alert latency breach count must be at least 1")
    @Max(value = 100000, message = "Alert latency breach count must be at most 100000")
    private Integer alertLatencyBreachCount;

    @Min(value = 1, message = "Alert latency window must be at least 1 second")
    @Max(value = 86400, message = "Alert latency window must be at most 86400 seconds")
    private Integer alertLatencyWindowSec;
}