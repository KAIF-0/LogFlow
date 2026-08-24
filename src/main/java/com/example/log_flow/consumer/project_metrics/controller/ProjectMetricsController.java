package com.example.log_flow.consumer.project_metrics.controller;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.log_flow.auth.entity.User;
import com.example.log_flow.auth.repository.UserRepository;
import com.example.log_flow.common.exception.AppException;
import com.example.log_flow.common.response.ApiResponse;
import com.example.log_flow.consumer.project_metrics.dto.ProjectMetricsResponse;
import com.example.log_flow.consumer.project_metrics.entity.ProjectMetrics;
import com.example.log_flow.consumer.project_metrics.repository.ProjectMetricsRepository;
import com.example.log_flow.project.repository.ProjectRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/projects/{projectId}/metrics")
@SecurityRequirement(name = "bearerAuth")
public class ProjectMetricsController {

    private final ProjectMetricsRepository projectMetricsRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ProjectMetricsController(ProjectMetricsRepository projectMetricsRepository,
                                    ProjectRepository projectRepository,
                                    UserRepository userRepository,
                                    ObjectMapper objectMapper) {
        this.projectMetricsRepository = projectMetricsRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ProjectMetricsResponse>> getMetrics(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long projectId
    ) {
        requireOwnedProject(userDetails.getUsername(), projectId);
        ProjectMetrics metrics = projectMetricsRepository.findById(projectId)
                .orElseGet(() -> {
                    ProjectMetrics created = new ProjectMetrics();
                    created.setProjectId(projectId);
                    return created;
                });

        ProjectMetricsResponse response = new ProjectMetricsResponse(
                metrics.getProjectId(),
                metrics.getTotal(),
                metrics.getSuccess(),
                metrics.getFailure(),
                metrics.getLatencySum(),
                readStatusCounts(metrics.getStatusCountsJson()),
                readEndpointCounts(metrics.getEndpointCountsJson())
        );

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    private void requireOwnedProject(String email, Long projectId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "user_not_found", "User not found"));
        if (projectRepository.findByIdAndUserId(projectId, user.getId()).isEmpty()) {
            throw new AppException(HttpStatus.NOT_FOUND, "project_not_found", "Project not found");
        }
    }

    private Map<Integer, Long> readStatusCounts(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<Integer, Long>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    private Map<String, Long> readEndpointCounts(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Long>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }
}
