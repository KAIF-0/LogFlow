package com.example.log_flow.consumer.persistence.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.log_flow.auth.entity.User;
import com.example.log_flow.auth.repository.UserRepository;
import com.example.log_flow.common.exception.AppException;
import com.example.log_flow.consumer.persistence.entity.ProjectLog;
import com.example.log_flow.consumer.persistence.repository.ProjectLogRepository;
import com.example.log_flow.project.repository.ProjectRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class ProjectLogQueryService {

    private final ProjectLogRepository projectLogRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectLogQueryService(ProjectLogRepository projectLogRepository,
                                  ProjectRepository projectRepository,
                                  UserRepository userRepository) {
        this.projectLogRepository = projectLogRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public Page<ProjectLog> getLogs(String email,
                                   Long projectId,
                                   List<Long> serviceIds,
                                   List<String> methods,
                                   List<Integer> statusCodes,
                                   String pathSearch,
                                   Pageable pageable) {
        requireOwnedProject(email, projectId);

        Specification<ProjectLog> spec = (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("projectId"), projectId);

            if (serviceIds != null && !serviceIds.isEmpty()) {
                predicate = cb.and(predicate, root.get("serviceId").in(serviceIds));
            }
            if (methods != null && !methods.isEmpty()) {
                predicate = cb.and(predicate, root.get("method").in(methods));
            }
            if (statusCodes != null && !statusCodes.isEmpty()) {
                predicate = cb.and(predicate, root.get("statusCode").in(statusCodes));
            }
            if (pathSearch != null && !pathSearch.isBlank()) {
                predicate = cb.and(predicate, cb.like(root.get("path"), "%" + pathSearch + "%"));
            }
            return predicate;
        };

        return projectLogRepository.findAll(spec, pageable);
    }

    private void requireOwnedProject(String email, Long projectId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "user_not_found", "User not found"));
        if (projectRepository.findByIdAndUserId(projectId, user.getId()).isEmpty()) {
            throw new AppException(HttpStatus.NOT_FOUND, "project_not_found", "Project not found");
        }
    }
}
