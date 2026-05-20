package com.example.log_flow.project.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectServiceResponse {

    private Long id;
    private Long projectId;
    private String name;
    private String basePath;
    private String description;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
