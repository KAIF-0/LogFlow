package com.example.log_flow.consumer.alert.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAlertResponse {

    private Long id;
    private Long projectId;
    private String alertType;
    private String message;
    private Integer triggeredCount;
    private Integer timeWindowSec;
    private String sentTo;
    private String status;
    private Instant createdAt;
}
