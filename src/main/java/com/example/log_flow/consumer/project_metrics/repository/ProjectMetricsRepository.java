package com.example.log_flow.consumer.project_metrics.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.log_flow.consumer.project_metrics.entity.ProjectMetrics;

public interface ProjectMetricsRepository extends JpaRepository<ProjectMetrics, Long> {
}
