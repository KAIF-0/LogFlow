package com.example.log_flow.project.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.log_flow.common.exception.AppException;
import com.example.log_flow.auth.entity.User;
import com.example.log_flow.auth.repository.UserRepository;
import com.example.log_flow.project.dto.ProjectServiceRequest;
import com.example.log_flow.project.dto.ProjectServiceResponse;
import com.example.log_flow.project.dto.UpdateProjectServiceRequest;
import com.example.log_flow.project.entity.ProjectServiceConfig;
import com.example.log_flow.project.mapper.ProjectServiceMapper;
import com.example.log_flow.project.repository.ProjectServiceRepository;
import com.example.log_flow.project.repository.ProjectRepository;

@Service
public class ProjectServiceManager {

    public static final String DEFAULT_SERVICE_NAME = "default-service";
    public static final String DEFAULT_BASE_PATH = "/";

    private final ProjectServiceRepository projectServiceRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectServiceManager(ProjectServiceRepository projectServiceRepository,
                                 ProjectRepository projectRepository,
                                 UserRepository userRepository) {
        this.projectServiceRepository = projectServiceRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public ProjectServiceResponse create(String email, Long projectId, ProjectServiceRequest request) {
        requireOwnedProject(email, projectId);
        String basePath = normalizeBasePath(request.getBasePath());
        if (projectServiceRepository.existsByProjectIdAndName(projectId, request.getName())) {
            throw new AppException(HttpStatus.CONFLICT, "service_name_exists", "Service name already exists");
        }
        if (projectServiceRepository.existsByProjectIdAndBasePath(projectId, basePath)) {
            throw new AppException(HttpStatus.CONFLICT, "base_path_exists", "Base path already exists");
        }
        ProjectServiceConfig service = new ProjectServiceConfig();
        service.setProjectId(projectId);
        service.setName(request.getName());
        service.setBasePath(basePath);
        service.setDescription(request.getDescription());
        service.setActive(request.getIsActive() == null || request.getIsActive());
        service.setCreatedAt(Instant.now());
        service.setUpdatedAt(Instant.now());
        projectServiceRepository.save(service);
        return ProjectServiceMapper.toResponse(service);
    }

    public List<ProjectServiceResponse> list(String email, Long projectId) {
        requireOwnedProject(email, projectId);
        return getServicesForProject(projectId).stream()
                .sorted(Comparator.comparing(ProjectServiceConfig::getBasePath).reversed())
                .map(ProjectServiceMapper::toResponse)
                .toList();
    }

    public ProjectServiceResponse get(String email, Long projectId, Long serviceId) {
        requireOwnedProject(email, projectId);
        ProjectServiceConfig service = requireService(projectId, serviceId);
        return ProjectServiceMapper.toResponse(service);
    }

    public ProjectServiceResponse update(String email, Long projectId, Long serviceId, UpdateProjectServiceRequest request) {
        requireOwnedProject(email, projectId);
        ProjectServiceConfig service = requireService(projectId, serviceId);
        boolean changed = false;

        if (request.getName() != null) {
            if (request.getName().isBlank()) {
                throw new AppException(HttpStatus.BAD_REQUEST, "invalid_service_name", "Service name is required");
            }
            if (!request.getName().equals(service.getName())
                    && projectServiceRepository.existsByProjectIdAndName(projectId, request.getName())) {
                throw new AppException(HttpStatus.CONFLICT, "service_name_exists", "Service name already exists");
            }
            service.setName(request.getName());
            changed = true;
        }

        if (request.getBasePath() != null) {
            String basePath = normalizeBasePath(request.getBasePath());
            if (isDefaultService(service)) {
                throw new AppException(HttpStatus.BAD_REQUEST, "default_service_locked", "Default service base path cannot be changed");
            }
            if (!basePath.equals(service.getBasePath())
                    && projectServiceRepository.existsByProjectIdAndBasePath(projectId, basePath)) {
                throw new AppException(HttpStatus.CONFLICT, "base_path_exists", "Base path already exists");
            }
            service.setBasePath(basePath);
            changed = true;
        }

        if (request.getDescription() != null) {
            service.setDescription(request.getDescription());
            changed = true;
        }

        if (request.getIsActive() != null) {
            service.setActive(request.getIsActive());
            changed = true;
        }

        if (!changed) {
            throw new AppException(HttpStatus.BAD_REQUEST, "no_updates", "No updates provided");
        }

        service.setUpdatedAt(Instant.now());
        projectServiceRepository.save(service);
        return ProjectServiceMapper.toResponse(service);
    }

    public void delete(String email, Long projectId, Long serviceId) {
        requireOwnedProject(email, projectId);
        ProjectServiceConfig service = requireService(projectId, serviceId);
        if (isDefaultService(service)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "default_service_locked", "Default service cannot be deleted");
        }
        projectServiceRepository.delete(service);
    }

    public ProjectServiceConfig createDefaultService(Long projectId) {
        ProjectServiceConfig existing = projectServiceRepository.findByProjectIdAndName(projectId, DEFAULT_SERVICE_NAME)
                .orElse(null);
        if (existing != null) {
            return existing;
        }
        ProjectServiceConfig service = new ProjectServiceConfig();
        service.setProjectId(projectId);
        service.setName(DEFAULT_SERVICE_NAME);
        service.setBasePath(DEFAULT_BASE_PATH);
        service.setActive(true);
        service.setCreatedAt(Instant.now());
        service.setUpdatedAt(Instant.now());
        return projectServiceRepository.save(service);
    }

    public List<ProjectServiceConfig> getServicesForProject(Long projectId) {
        List<ProjectServiceConfig> services = projectServiceRepository.findAllByProjectId(projectId);
        if (services.isEmpty() && projectRepository.existsById(projectId)) {
            createDefaultService(projectId);
            services = projectServiceRepository.findAllByProjectId(projectId);
        }
        return services;
    }

    private void requireOwnedProject(String email, Long projectId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "user_not_found", "User not found"));
        if (!projectRepository.findByIdAndUserId(projectId, user.getId()).isPresent()) {
            throw new AppException(HttpStatus.NOT_FOUND, "project_not_found", "Project not found");
        }
    }

    private ProjectServiceConfig requireService(Long projectId, Long serviceId) {
        return projectServiceRepository.findByProjectIdAndId(projectId, serviceId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "service_not_found", "Service not found"));
    }

    private boolean isDefaultService(ProjectServiceConfig service) {
        return DEFAULT_BASE_PATH.equals(service.getBasePath())
                && DEFAULT_SERVICE_NAME.equals(service.getName());
    }

    private String normalizeBasePath(String basePath) {
        if (basePath == null || basePath.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "invalid_base_path", "Base path is required");
        }
        String normalized = basePath.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
