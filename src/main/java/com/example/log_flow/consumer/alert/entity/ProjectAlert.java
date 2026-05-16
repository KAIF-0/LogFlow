package com.example.log_flow.consumer.alert.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "project_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "alert_type", nullable = false)
    private String alertType;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(name = "triggered_count", nullable = false)
    private Integer triggeredCount;

    @Column(name = "time_window_sec", nullable = false)
    private Integer timeWindowSec;

    @Column(name = "sent_to", nullable = false)
    private String sentTo;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}