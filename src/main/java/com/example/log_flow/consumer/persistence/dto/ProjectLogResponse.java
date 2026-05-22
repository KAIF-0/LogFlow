package com.example.log_flow.consumer.persistence.dto;

import java.time.Instant;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectLogResponse {

    private Long id;
    private Long projectId;
    private Long serviceId;
    private String requestId;
    private String method;
    private String path;
    private Integer statusCode;
    private Map<String, String> requestHeaders;
    private String requestBody;
    private Map<String, String> responseHeaders;
    private String responseBody;
    private Long latencyMs;
    private String errorMessage;
    private Instant timestamp;
}
