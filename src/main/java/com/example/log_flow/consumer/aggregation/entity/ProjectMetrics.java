package com.example.log_flow.consumer.aggregation.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "project_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMetrics {

    @Id
    @Column(name = "project_id")
    private Long projectId;

    @Column(nullable = false)
    private Long total = 0L;

    @Column(nullable = false)
    private Long success = 0L;

    @Column(nullable = false)
    private Long failure = 0L;

    @Column(name = "latency_sum", nullable = false)
    private Long latencySum = 0L;

    @Column(name = "status_counts", columnDefinition = "text")
    private String statusCountsJson;

    @Column(name = "endpoint_counts", columnDefinition = "text")
    private String endpointCountsJson;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
