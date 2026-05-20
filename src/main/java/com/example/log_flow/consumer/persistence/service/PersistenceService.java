package com.example.log_flow.consumer.persistence.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.log_flow.consumer.common.service.IngestionEventService;
import com.example.log_flow.consumer.persistence.entity.ProjectLog;
import com.example.log_flow.consumer.persistence.repository.ProjectLogRepository;
import com.example.log_flow.ingestion.dto.LifecycleLogRequest;
import com.example.log_flow.ingestion.dto.ValidatedLogBatchMessage;
import com.example.log_flow.project.entity.ProjectServiceConfig;
import com.example.log_flow.project.service.ProjectServiceManager;
import com.example.log_flow.project.service.ProjectServiceMatcher;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PersistenceService {

    private static final int MAX_RETRIES = 3;

    private final ProjectLogRepository projectLogRepository;
    private final IngestionEventService ingestionEventService;
    private final ObjectMapper objectMapper;
    private final ProjectServiceManager projectServiceManager;
    private final ProjectServiceMatcher projectServiceMatcher;

    public PersistenceService(ProjectLogRepository projectLogRepository,
                              IngestionEventService ingestionEventService,
                              ObjectMapper objectMapper,
                              ProjectServiceManager projectServiceManager,
                              ProjectServiceMatcher projectServiceMatcher) {
        this.projectLogRepository = projectLogRepository;
        this.ingestionEventService = ingestionEventService;
        this.objectMapper = objectMapper;
        this.projectServiceManager = projectServiceManager;
        this.projectServiceMatcher = projectServiceMatcher;
    }

    public void persist(ValidatedLogBatchMessage message) {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                List<ProjectServiceConfig> services = projectServiceManager.getServicesForProject(message.getProjectId());
                List<ProjectLog> logs = message.getLogs().stream()
                    .map(log -> map(message.getProjectId(), log, services))
                        .toList();
                projectLogRepository.saveAll(logs);
                return;
            } catch (Exception e) {
                attempts++;
                if (attempts >= MAX_RETRIES) {
                    ingestionEventService.recordFailure(
                            message.getProjectId(),
                            "PERSISTENCE_FAILURE",
                            "log.persistence.queue",
                            message.getLogs().size(),
                            attempts,
                            e.getMessage()
                    );
                }
            }
        }
    }

    private ProjectLog map(Long projectId, LifecycleLogRequest log, List<ProjectServiceConfig> services) {
        ProjectLog entity = new ProjectLog();
        entity.setProjectId(projectId);
        ProjectServiceConfig resolved = projectServiceMatcher.resolveService(services, log.getPath());
        if (resolved != null) {
            entity.setServiceId(resolved.getId());
        }
        entity.setRequestId(log.getRequestId());
        entity.setMethod(log.getMethod());
        entity.setPath(log.getPath());
        entity.setStatusCode(log.getStatusCode());
        entity.setRequestHeaders(writeJson(log.getRequestHeaders()));
        entity.setRequestBody(log.getRequestBody());
        entity.setResponseHeaders(writeJson(log.getResponseHeaders()));
        entity.setResponseBody(log.getResponseBody());
        entity.setLatencyMs(log.getLatencyMs());
        entity.setErrorMessage(log.getErrorMessage());
        entity.setTimestamp(log.getTimestamp());
        return entity;
    }

    private String writeJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }
}