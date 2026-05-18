package com.example.log_flow.project.dto;

import com.example.log_flow.common.enums.ProjectEnvironment;
import com.example.log_flow.common.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private String alertEmail;
    private ProjectEnvironment environment;
    private ProjectStatus status;
    private boolean ingestionEnabled;
    private String apiKeyPrefix;
    private String apiKey;
    private Instant createdAt;
    private Instant updatedAt;
}