package com.example.log_flow.ingestion.dto;

public class ProjectContext {

    private final Long projectId;

    public ProjectContext(Long projectId) {
        this.projectId = projectId;
    }

    public Long getProjectId() {
        return projectId;
    }
}