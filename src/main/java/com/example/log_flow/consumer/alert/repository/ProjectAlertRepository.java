package com.example.log_flow.consumer.alert.repository;

import com.example.log_flow.consumer.alert.entity.ProjectAlert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectAlertRepository extends JpaRepository<ProjectAlert, Long> {
}