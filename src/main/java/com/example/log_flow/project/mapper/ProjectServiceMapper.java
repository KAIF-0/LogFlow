package com.example.log_flow.project.mapper;

import com.example.log_flow.project.dto.ProjectServiceResponse;
import com.example.log_flow.project.entity.ProjectServiceConfig;

public class ProjectServiceMapper {

    public static ProjectServiceResponse toResponse(ProjectServiceConfig service) {
        if (service == null) return null;
        return new ProjectServiceResponse(
                service.getId(),
                service.getProjectId(),
                service.getName(),
                service.getBasePath(),
                service.getDescription(),
                service.isActive(),
                service.getCreatedAt(),
                service.getUpdatedAt()
        );
    }
}
