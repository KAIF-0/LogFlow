package com.example.log_flow.consumer.alert.controller;

import com.example.log_flow.auth.entity.User;
import com.example.log_flow.auth.repository.UserRepository;
import com.example.log_flow.common.exception.AppException;
import com.example.log_flow.common.response.ApiResponse;
import com.example.log_flow.common.response.Pagination;
import com.example.log_flow.consumer.alert.dto.ProjectAlertResponse;
import com.example.log_flow.consumer.alert.entity.ProjectAlert;
import com.example.log_flow.consumer.alert.repository.ProjectAlertRepository;
import com.example.log_flow.project.repository.ProjectRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}/alerts")
@SecurityRequirement(name = "bearerAuth")
public class ProjectAlertsController {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final ProjectAlertRepository alertRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectAlertsController(ProjectAlertRepository alertRepository,
                                   ProjectRepository projectRepository,
                                   UserRepository userRepository) {
        this.alertRepository = alertRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectAlertResponse>>> getAlerts(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long projectId,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "limit", required = false, defaultValue = "20") int limit
    ) {
        int resolvedPage = Math.max(page, DEFAULT_PAGE);
        int resolvedLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
        if (limit > MAX_LIMIT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "limit_exceeded", "Limit must be at most " + MAX_LIMIT);
        }

        requireOwnedProject(userDetails.getUsername(), projectId);
        PageRequest pageable = PageRequest.of(resolvedPage - 1, resolvedLimit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProjectAlert> alerts = alertRepository.findByProjectId(projectId, pageable);

        List<ProjectAlertResponse> response = alerts.getContent().stream()
                .map(this::mapAlert)
                .toList();

        Pagination pagination = new Pagination(
                resolvedPage,
                resolvedLimit,
                alerts.getTotalElements(),
                alerts.getTotalPages(),
                alerts.hasNext(),
                alerts.hasPrevious()
        );

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response, pagination));
    }

    private void requireOwnedProject(String email, Long projectId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "user_not_found", "User not found"));
        if (projectRepository.findByIdAndUserId(projectId, user.getId()).isEmpty()) {
            throw new AppException(HttpStatus.NOT_FOUND, "project_not_found", "Project not found");
        }
    }

    private ProjectAlertResponse mapAlert(ProjectAlert alert) {
        return new ProjectAlertResponse(
                alert.getId(),
                alert.getProjectId(),
                alert.getAlertType(),
                alert.getMessage(),
                alert.getTriggeredCount(),
                alert.getTimeWindowSec(),
                alert.getSentTo(),
                alert.getStatus(),
                alert.getCreatedAt()
        );
    }
}
