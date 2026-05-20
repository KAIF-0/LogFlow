package com.example.log_flow.project.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.log_flow.project.entity.ProjectServiceConfig;

@Component
public class ProjectServiceMatcher {

    public ProjectServiceConfig resolveService(List<ProjectServiceConfig> services, String path) {
        if (services == null || services.isEmpty()) {
            return null;
        }
        String normalizedPath = normalizePath(path);
        ProjectServiceConfig match = services.stream()
                .filter(ProjectServiceConfig::isActive)
                .filter(service -> normalizedPath.startsWith(service.getBasePath()))
                .max(Comparator.comparingInt(service -> service.getBasePath().length()))
            .orElse(null);
        if (match != null) {
            return match;
        }
        return services.stream()
            .filter(service -> ProjectServiceManager.DEFAULT_BASE_PATH.equals(service.getBasePath()))
            .findFirst()
            .orElse(null);
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        String normalized = path.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized;
    }
}
