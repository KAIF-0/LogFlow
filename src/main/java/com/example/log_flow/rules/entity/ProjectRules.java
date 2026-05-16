package com.example.log_flow.rules.entity;

import com.example.log_flow.project.entity.Project;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "project_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private Project project;

    @Column(name = "max_payload_size_kb", nullable = false)
    private Integer maxPayloadSizeKb;

    @Column(name = "blocked_routes", columnDefinition = "text")
    private String blockedRoutesJson;

    @Column(name = "masked_fields", columnDefinition = "text")
    private String maskedFieldsJson;

    @Column(name = "header_whitelist", columnDefinition = "text")
    private String headerWhitelistJson;

    @Column(name = "sampling_rate", nullable = false)
    private Integer samplingRate;

    @Column(name = "rate_limit_per_min", nullable = false)
    private Integer rateLimitPerMin;

    @Column(name = "alert_failure_threshold")
    private Integer alertFailureThreshold;

    @Column(name = "alert_time_window_sec")
    private Integer alertTimeWindowSec;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();
}