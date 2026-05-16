package com.example.log_flow.rules.service;

import com.example.log_flow.auth.entity.User;
import com.example.log_flow.auth.repository.UserRepository;
import com.example.log_flow.common.exception.AppException;
import com.example.log_flow.project.entity.Project;
import com.example.log_flow.project.repository.ProjectRepository;
import com.example.log_flow.rules.dto.RulesRequest;
import com.example.log_flow.rules.dto.RulesResponse;
import com.example.log_flow.rules.entity.ProjectRules;
import com.example.log_flow.rules.mapper.RulesMapper;
import com.example.log_flow.rules.repository.ProjectRulesRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RulesService {

    private static final List<String> EMPTY_LIST = List.of();
    private static final int DEFAULT_MAX_PAYLOAD_KB = 256;
    private static final int DEFAULT_SAMPLING_RATE = 100;
    private static final int DEFAULT_RATE_LIMIT_PER_MIN = 60;

    private final ProjectRulesRepository rulesRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public RulesService(ProjectRulesRepository rulesRepository,
                        ProjectRepository projectRepository,
                        UserRepository userRepository,
                        ObjectMapper objectMapper) {
        this.rulesRepository = rulesRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public RulesResponse getRules(String email, Long projectId) {
        Project project = requireOwnedProject(email, projectId);
        ProjectRules rules = rulesRepository.findByProjectId(project.getId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "rules_not_found", "Rules not found"));
        return toResponse(rules);
    }

    public RulesResponse upsertRules(String email, Long projectId, RulesRequest request) {
        Project project = requireOwnedProject(email, projectId);
        ProjectRules rules = rulesRepository.findByProjectId(project.getId())
                .orElseGet(() -> createDefaults(project));

        rules.setMaxPayloadSizeKb(request.getMaxPayloadSizeKb());
        rules.setBlockedRoutesJson(writeList(request.getBlockedRoutes()));
        rules.setMaskedFieldsJson(writeList(request.getMaskedFields()));
        rules.setHeaderWhitelistJson(writeList(request.getHeaderWhitelist()));
        rules.setSamplingRate(request.getSamplingRate());
        rules.setRateLimitPerMin(request.getRateLimitPerMin());
        rules.setAlertFailureThreshold(request.getAlertFailureThreshold());
        rules.setAlertTimeWindowSec(request.getAlertTimeWindowSec());
        rules.setUpdatedAt(Instant.now());

        rulesRepository.save(rules);
        return toResponse(rules);
    }

    public void createDefaultsForProject(Project project) {
        rulesRepository.findByProjectId(project.getId()).orElseGet(() -> createDefaults(project));
    }

    private ProjectRules createDefaults(Project project) {
        ProjectRules rules = new ProjectRules();
        rules.setProject(project);
        rules.setMaxPayloadSizeKb(DEFAULT_MAX_PAYLOAD_KB);
        rules.setBlockedRoutesJson(writeList(EMPTY_LIST));
        rules.setMaskedFieldsJson(writeList(EMPTY_LIST));
        rules.setHeaderWhitelistJson(writeList(EMPTY_LIST));
        rules.setSamplingRate(DEFAULT_SAMPLING_RATE);
        rules.setRateLimitPerMin(DEFAULT_RATE_LIMIT_PER_MIN);
        rules.setAlertFailureThreshold(null);
        rules.setAlertTimeWindowSec(null);
        rules.setCreatedAt(Instant.now());
        rules.setUpdatedAt(Instant.now());
        return rulesRepository.save(rules);
    }

    private RulesResponse toResponse(ProjectRules rules) {
        List<String> blocked = readList(rules.getBlockedRoutesJson());
        List<String> masked = readList(rules.getMaskedFieldsJson());
        List<String> headers = readList(rules.getHeaderWhitelistJson());
        return RulesMapper.toResponse(rules, blocked, masked, headers);
    }

    private String writeList(List<String> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? EMPTY_LIST : value);
        } catch (JsonProcessingException e) {
            throw new AppException(HttpStatus.BAD_REQUEST, "invalid_rules", "Invalid rules format");
        }
    }

    private List<String> readList(String value) {
        if (value == null || value.isBlank()) return EMPTY_LIST;
        try {
            return objectMapper.readValue(value, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return EMPTY_LIST;
        }
    }

    private Project requireOwnedProject(String email, Long projectId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "user_not_found", "User not found"));
        return projectRepository.findByIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "project_not_found", "Project not found"));
    }
}