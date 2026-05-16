package com.example.log_flow.project.service;

import com.example.log_flow.auth.entity.User;
import com.example.log_flow.auth.repository.UserRepository;
import com.example.log_flow.common.enums.ProjectStatus;
import com.example.log_flow.common.exception.AppException;
import com.example.log_flow.project.dto.CreateProjectRequest;
import com.example.log_flow.project.dto.IngestionStatusRequest;
import com.example.log_flow.project.dto.ProjectResponse;
import com.example.log_flow.project.dto.UpdateProjectRequest;
import com.example.log_flow.project.entity.Project;
import com.example.log_flow.project.mapper.ProjectMapper;
import com.example.log_flow.project.repository.ProjectRepository;
import com.example.log_flow.rules.service.RulesService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;


@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectKeyService projectKeyService;
    private final RulesService rulesService;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository,
                          ProjectKeyService projectKeyService,
                          RulesService rulesService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectKeyService = projectKeyService;
        this.rulesService = rulesService;
    }

    public ProjectResponse create(String email, CreateProjectRequest req) {
        User user = requireUser(email);
        Project project = new Project();
        project.setUser(user);
        project.setName(req.getName());
        project.setDescription(req.getDescription());
        project.setEnvironment(req.getEnvironment());
        project.setStatus(ProjectStatus.ACTIVE);
        project.setIngestionEnabled(true);
        project.setCreatedAt(Instant.now());
        project.setUpdatedAt(Instant.now());
        projectRepository.save(project);
        String rawKey = projectKeyService.generateRawKey(project.getId());
        project.setApiKeyHash(projectKeyService.hashKey(rawKey));
        project.setApiKeyPrefix(projectKeyService.prefix(rawKey));
        project.setUpdatedAt(Instant.now());
        projectRepository.save(project);
        rulesService.createDefaultsForProject(project);
        return ProjectMapper.toResponse(project, rawKey);
    }

    public List<ProjectResponse> list(String email) {
        User user = requireUser(email);
        return projectRepository.findByUserId(user.getId()).stream()
                .map(project -> ProjectMapper.toResponse(project, null))
                .toList();
    }

    public ProjectResponse getById(String email, Long id) {
        Project project = requireOwnedProject(email, id);
        return ProjectMapper.toResponse(project, null);
    }

    public ProjectResponse update(String email, Long id, UpdateProjectRequest req) {
        Project project = requireOwnedProject(email, id);
        boolean changed = false;

        if (req.getName() != null) {
            if (req.getName().isBlank()) {
                throw new AppException(HttpStatus.BAD_REQUEST, "invalid_name", "Project name is required");
            }
            project.setName(req.getName());
            changed = true;
        }

        if (req.getDescription() != null) {
            project.setDescription(req.getDescription());
            changed = true;
        }

        if (req.getEnvironment() != null) {
            project.setEnvironment(req.getEnvironment());
            changed = true;
        }

        if (!changed) {
            throw new AppException(HttpStatus.BAD_REQUEST, "no_updates", "No updates provided");
        }

        project.setUpdatedAt(Instant.now());
        projectRepository.save(project);
        return ProjectMapper.toResponse(project, null);
    }

    public ProjectResponse regenerateKey(String email, Long id) {
        Project project = requireOwnedProject(email, id);
        String rawKey = projectKeyService.generateRawKey(project.getId());
        project.setApiKeyHash(projectKeyService.hashKey(rawKey));
        project.setApiKeyPrefix(projectKeyService.prefix(rawKey));
        project.setUpdatedAt(Instant.now());
        projectRepository.save(project);
        return ProjectMapper.toResponse(project, rawKey);
    }

    public ProjectResponse updateIngestion(String email, Long id, IngestionStatusRequest req) {
        Project project = requireOwnedProject(email, id);
        project.setIngestionEnabled(req.getIngestionEnabled());
        project.setUpdatedAt(Instant.now());
        projectRepository.save(project);
        return ProjectMapper.toResponse(project, null);
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "user_not_found", "User not found"));
    }

    private Project requireOwnedProject(String email, Long projectId) {
        User user = requireUser(email);
        return projectRepository.findByIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "project_not_found", "Project not found"));
    }
}