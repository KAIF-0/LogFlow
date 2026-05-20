package com.example.log_flow.consumer.service_metrics.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "service_metrics",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"project_id", "service_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

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
