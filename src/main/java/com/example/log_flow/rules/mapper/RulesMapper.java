package com.example.log_flow.rules.mapper;

import java.util.List;

import com.example.log_flow.rules.dto.RulesResponse;
import com.example.log_flow.rules.entity.ProjectRules;

public class RulesMapper {

    public static RulesResponse toResponse(ProjectRules rules,
                                           List<String> blockedRoutes,
                                           List<String> maskedFields,
                                           List<String> headerWhitelist) {
        if (rules == null) return null;
        return new RulesResponse(
                rules.getProject().getId(),
                rules.getMaxPayloadSizeKb(),
                blockedRoutes,
                maskedFields,
                headerWhitelist,
                rules.getSamplingRate(),
                rules.getRateLimitPerMin(),
                rules.getAlertFailureThreshold(),
                rules.getAlertTimeWindowSec(),
                rules.getAlertLatencyThresholdMs(),
                rules.getAlertLatencyBreachCount(),
                rules.getAlertLatencyWindowSec(),
                rules.getCreatedAt(),
                rules.getUpdatedAt()
        );
    }
}