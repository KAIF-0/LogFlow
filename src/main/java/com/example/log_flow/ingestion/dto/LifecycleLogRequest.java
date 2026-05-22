package com.example.log_flow.ingestion.dto;

import java.time.Instant;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LifecycleLogRequest {

    @NotBlank
        @jakarta.validation.constraints.Size(max = 128, message = "Request id must be at most 128 characters")
    private String requestId;

    @NotBlank
        @jakarta.validation.constraints.Pattern(
            regexp = "^(GET|POST|PUT|PATCH|DELETE|OPTIONS|HEAD)$",
            message = "Method must be a valid HTTP method"
        )
    private String method;

    @NotBlank
        @jakarta.validation.constraints.Size(max = 2048, message = "Path must be at most 2048 characters")
        @jakarta.validation.constraints.Pattern(
            regexp = "^/.*",
            message = "Path must start with /"
        )
    private String path;

    @NotNull
        @jakarta.validation.constraints.Min(value = 100, message = "Status code must be at least 100")
        @jakarta.validation.constraints.Max(value = 599, message = "Status code must be at most 599")
    private Integer statusCode;

    private Map<String, String> requestHeaders;
    private String requestBody;
    private Map<String, String> responseHeaders;
    private String responseBody;

    @NotNull
    @jakarta.validation.constraints.Min(value = 0, message = "Latency must be at least 0")
    @jakarta.validation.constraints.Max(value = 600000, message = "Latency must be at most 600000 ms")
    private Long latencyMs;

    @NotNull
    @jakarta.validation.constraints.PastOrPresent(message = "Timestamp cannot be in the future")
    private Instant timestamp;

    private String errorMessage;
}