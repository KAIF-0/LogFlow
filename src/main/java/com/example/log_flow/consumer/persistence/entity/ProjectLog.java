package com.example.log_flow.consumer.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "project_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "request_id", nullable = false)
    private String requestId;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false)
    private String path;

    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    @Column(name = "request_headers", columnDefinition = "text")
    private String requestHeaders;

    @Column(name = "request_body", columnDefinition = "text")
    private String requestBody;

    @Column(name = "response_headers", columnDefinition = "text")
    private String responseHeaders;

    @Column(name = "response_body", columnDefinition = "text")
    private String responseBody;

    @Column(name = "latency_ms", nullable = false)
    private Long latencyMs;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(nullable = false)
    private Instant timestamp;
}