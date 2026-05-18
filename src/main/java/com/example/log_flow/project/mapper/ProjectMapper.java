package com.example.log_flow.project.mapper;

import com.example.log_flow.project.dto.ProjectResponse;
import com.example.log_flow.project.entity.Project;

public class ProjectMapper {

    public static ProjectResponse toResponse(Project project, String apiKey) {
        if (project == null) return null;
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getAlertEmail(),
                project.getEnvironment(),
                project.getStatus(),
                project.isIngestionEnabled(),
                project.getApiKeyPrefix(),
                apiKey,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}