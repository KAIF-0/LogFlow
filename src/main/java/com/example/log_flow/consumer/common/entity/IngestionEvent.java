package com.example.log_flow.consumer.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "ingestion_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IngestionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "queue_name", nullable = false)
    private String queueName;

    @Column(name = "batch_size", nullable = false)
    private Integer batchSize;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}