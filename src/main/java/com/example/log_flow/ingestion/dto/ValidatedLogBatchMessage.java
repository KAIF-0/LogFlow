package com.example.log_flow.ingestion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidatedLogBatchMessage {

    private Long projectId;
    private Instant receivedAt;
    private List<LifecycleLogRequest> logs;
}