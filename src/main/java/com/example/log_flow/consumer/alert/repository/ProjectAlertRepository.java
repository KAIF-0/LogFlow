package com.example.log_flow.consumer.alert.repository;

import com.example.log_flow.consumer.alert.entity.ProjectAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectAlertRepository extends JpaRepository<ProjectAlert, Long> {
	Page<ProjectAlert> findByProjectId(Long projectId, Pageable pageable);
}