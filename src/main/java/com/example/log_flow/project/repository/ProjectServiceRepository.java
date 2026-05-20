package com.example.log_flow.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.log_flow.project.entity.ProjectServiceConfig;

public interface ProjectServiceRepository extends JpaRepository<ProjectServiceConfig, Long> {
    List<ProjectServiceConfig> findAllByProjectId(Long projectId);

    Optional<ProjectServiceConfig> findByProjectIdAndId(Long projectId, Long id);

    Optional<ProjectServiceConfig> findByProjectIdAndName(Long projectId, String name);

    Optional<ProjectServiceConfig> findByProjectIdAndBasePath(Long projectId, String basePath);

    boolean existsByProjectIdAndName(Long projectId, String name);

    boolean existsByProjectIdAndBasePath(Long projectId, String basePath);
}
