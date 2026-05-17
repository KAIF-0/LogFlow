package com.example.log_flow.consumer.aggregation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.log_flow.consumer.aggregation.entity.ProjectMetrics;

public interface ProjectMetricsRepository extends JpaRepository<ProjectMetrics, Long> {
}
