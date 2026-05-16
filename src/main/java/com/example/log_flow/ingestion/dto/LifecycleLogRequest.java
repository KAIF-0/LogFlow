package com.example.log_flow.ingestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LifecycleLogRequest {

    @NotBlank
    private String requestId;

    @NotBlank
    private String method;

    @NotBlank
    private String path;

    @NotNull
    private Integer statusCode;

    private Map<String, String> requestHeaders;
    private String requestBody;
    private Map<String, String> responseHeaders;
    private String responseBody;

    @NotNull
    private Long latencyMs;

    @NotNull
    private Instant timestamp;

    private String errorMessage;
}