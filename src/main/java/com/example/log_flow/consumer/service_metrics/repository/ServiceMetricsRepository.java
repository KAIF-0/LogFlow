package com.example.log_flow.consumer.service_metrics.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.log_flow.consumer.service_metrics.entity.ServiceMetrics;

public interface ServiceMetricsRepository extends JpaRepository<ServiceMetrics, Long> {
    Optional<ServiceMetrics> findByProjectIdAndServiceId(Long projectId, Long serviceId);
}
