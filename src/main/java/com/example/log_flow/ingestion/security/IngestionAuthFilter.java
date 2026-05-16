package com.example.log_flow.ingestion.security;

import com.example.log_flow.common.response.ApiResponse;
import com.example.log_flow.common.response.ErrorInfo;
import com.example.log_flow.ingestion.dto.ProjectContext;
import com.example.log_flow.project.entity.Project;
import com.example.log_flow.project.repository.ProjectRepository;
import com.example.log_flow.common.enums.ProjectStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class IngestionAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-API-KEY";
    private static final String CONTEXT_KEY = "projectContext";

    private final IngestionRateLimitService rateLimitService;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public IngestionAuthFilter(IngestionRateLimitService rateLimitService,
                               ProjectRepository projectRepository,
                               PasswordEncoder passwordEncoder,
                               ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.projectRepository = projectRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String apiKey = request.getHeader(HEADER);
        if (apiKey == null || apiKey.isBlank()) {
            writeError(response, HttpStatus.UNAUTHORIZED, "api_key_missing", "API key required");
            return;
        }

        if (!rateLimitService.tryConsume(apiKey)) {
            writeError(response, HttpStatus.TOO_MANY_REQUESTS, "rate_limit_exceeded", "Rate limit exceeded");
            return;
        }

        Long projectId = extractProjectId(apiKey);
        if (projectId == null) {
            writeError(response, HttpStatus.UNAUTHORIZED, "api_key_invalid", "Invalid API key");
            return;
        }

        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            writeError(response, HttpStatus.UNAUTHORIZED, "project_not_found", "Project not found");
            return;
        }

        Project project = projectOpt.get();
        if (!passwordEncoder.matches(apiKey, project.getApiKeyHash())) {
            writeError(response, HttpStatus.UNAUTHORIZED, "api_key_invalid", "Invalid API key");
            return;
        }

        if (project.getStatus() != ProjectStatus.ACTIVE) {
            writeError(response, HttpStatus.FORBIDDEN, "project_inactive", "Project inactive");
            return;
        }

        if (!project.isIngestionEnabled()) {
            writeError(response, HttpStatus.FORBIDDEN, "ingestion_disabled", "Ingestion disabled");
            return;
        }

        request.setAttribute(CONTEXT_KEY, new ProjectContext(project.getId()));
        filterChain.doFilter(request, response);
    }

    private Long extractProjectId(String apiKey) {
        String[] parts = apiKey.split("_");
        if (parts.length < 3) return null;
        if (!"lf".equals(parts[0])) return null;
        try {
            return Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String code, String message) throws IOException {
        ErrorInfo errorInfo = new ErrorInfo(code, message, null);
        ApiResponse<Void> apiResponse = ApiResponse.failure(status.value(), errorInfo);
        response.setStatus(status.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}