package com.example.log_flow.consumer.project_metrics.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMetricsResponse {

    private Long projectId;
    private Long total;
    private Long success;
    private Long failure;
    private Long latencySum;
    private Map<Integer, Long> statusCounts;
    private Map<String, Long> endpointCounts;
}
