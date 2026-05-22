package com.example.log_flow.consumer.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.log_flow.consumer.persistence.entity.ProjectLog;

public interface ProjectLogRepository extends JpaRepository<ProjectLog, Long>, JpaSpecificationExecutor<ProjectLog> {
}