package com.example.log_flow.rules.repository;

import com.example.log_flow.rules.entity.ProjectRules;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRulesRepository extends JpaRepository<ProjectRules, Long> {
    Optional<ProjectRules> findByProjectId(Long projectId);
}