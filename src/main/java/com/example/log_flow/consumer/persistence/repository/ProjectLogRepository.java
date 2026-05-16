package com.example.log_flow.consumer.persistence.repository;

import com.example.log_flow.consumer.persistence.entity.ProjectLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectLogRepository extends JpaRepository<ProjectLog, Long> {
}