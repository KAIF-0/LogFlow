package com.example.log_flow.consumer.persistence.controller;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import com.example.log_flow.common.exception.AppException;
import com.example.log_flow.common.response.ApiResponse;
import com.example.log_flow.common.response.Pagination;
import com.example.log_flow.consumer.persistence.dto.ProjectLogResponse;
import com.example.log_flow.consumer.persistence.entity.ProjectLog;
import com.example.log_flow.consumer.persistence.service.ProjectLogQueryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/projects/{projectId}/logs")
@SecurityRequirement(name = "bearerAuth")
public class ProjectLogsController {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final ProjectLogQueryService projectLogQueryService;
    private final ObjectMapper objectMapper;

    public ProjectLogsController(ProjectLogQueryService projectLogQueryService, ObjectMapper objectMapper) {
        this.projectLogQueryService = projectLogQueryService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectLogResponse>>> getLogs(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long projectId,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "limit", required = false, defaultValue = "20") int limit,
            @RequestParam(name = "serviceIds", required = false) List<Long> serviceIds,
            @RequestParam(name = "methods", required = false) List<String> methods,
            @RequestParam(name = "statusCodes", required = false) List<Integer> statusCodes,
            @RequestParam(name = "path", required = false) String path,
            @RequestParam(name = "sortBy", required = false, defaultValue = "timestamp") String sortBy,
            @RequestParam(name = "sortDir", required = false, defaultValue = "desc") String sortDir
    ) {
        int resolvedPage = Math.max(page, DEFAULT_PAGE);
        int resolvedLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
        if (limit > MAX_LIMIT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "limit_exceeded", "Limit must be at most " + MAX_LIMIT);
        }

        String sortField = "timestamp".equalsIgnoreCase(sortBy) ? "timestamp" : "latencyMs";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageable = PageRequest.of(resolvedPage - 1, resolvedLimit, Sort.by(direction, sortField));

        Page<ProjectLog> logs = projectLogQueryService.getLogs(
                userDetails.getUsername(),
                projectId,
                serviceIds,
                methods,
                statusCodes,
                path,
                pageable
        );

        List<ProjectLogResponse> response = logs.getContent().stream()
                .map(this::mapLog)
                .toList();

        Pagination pagination = new Pagination(
                resolvedPage,
                resolvedLimit,
                logs.getTotalElements(),
                logs.getTotalPages(),
                logs.hasNext(),
                logs.hasPrevious()
        );

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response, pagination));
    }

    private ProjectLogResponse mapLog(ProjectLog log) {
        return new ProjectLogResponse(
                log.getId(),
                log.getProjectId(),
                log.getServiceId(),
                log.getRequestId(),
                log.getMethod(),
                log.getPath(),
                log.getStatusCode(),
                readMap(log.getRequestHeaders()),
                log.getRequestBody(),
                readMap(log.getResponseHeaders()),
                log.getResponseBody(),
                log.getLatencyMs(),
                log.getErrorMessage(),
                log.getTimestamp()
        );
    }

    private Map<String, String> readMap(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            return null;
        }
    }
}
