package com.example.log_flow.rules.mapper;

import com.example.log_flow.rules.dto.RulesResponse;
import com.example.log_flow.rules.entity.ProjectRules;

import java.util.List;

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
                rules.getCreatedAt(),
                rules.getUpdatedAt()
        );
    }
}