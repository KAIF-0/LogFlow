package com.example.log_flow.consumer.alert.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.example.log_flow.consumer.alert.entity.ProjectAlert;
import com.example.log_flow.consumer.alert.repository.ProjectAlertRepository;
import com.example.log_flow.consumer.common.service.EmailService;
import com.example.log_flow.consumer.common.service.IngestionEventService;
import com.example.log_flow.ingestion.dto.LifecycleLogRequest;
import com.example.log_flow.ingestion.dto.ValidatedLogBatchMessage;
import com.example.log_flow.project.entity.Project;
import com.example.log_flow.project.repository.ProjectRepository;
import com.example.log_flow.rules.entity.ProjectRules;
import com.example.log_flow.rules.repository.ProjectRulesRepository;

@Service
public class AlertService {

    private static final int MAX_RETRIES = 3;
    private static final int DEFAULT_WINDOW_SEC = 60;

    private final ProjectRulesRepository rulesRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAlertRepository alertRepository;
    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;
    private final IngestionEventService ingestionEventService;

    public AlertService(ProjectRulesRepository rulesRepository,
            ProjectRepository projectRepository,
            ProjectAlertRepository alertRepository,
            StringRedisTemplate redisTemplate,
            EmailService emailService,
            IngestionEventService ingestionEventService) {
        this.rulesRepository = rulesRepository;
        this.projectRepository = projectRepository;
        this.alertRepository = alertRepository;
        this.redisTemplate = redisTemplate;
        this.emailService = emailService;
        this.ingestionEventService = ingestionEventService;
    }

    public void process(ValidatedLogBatchMessage message) {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                ProjectRules rules = rulesRepository.findByProjectId(message.getProjectId()).orElse(null);
                if (rules == null) {
                    return;
                }

                Integer failureThreshold = rules.getAlertFailureThreshold();
                if (failureThreshold != null) {
                    int windowSec = rules.getAlertTimeWindowSec() == null ? DEFAULT_WINDOW_SEC : rules.getAlertTimeWindowSec();
                    String counterKey = "alerts:project:" + message.getProjectId() + ":error_window";
                    String lockKey = "alerts:project:" + message.getProjectId() + ":lock";

                    int failed = 0;
                    for (LifecycleLogRequest log : message.getLogs()) {
                        if (log.getStatusCode() >= 500) {
                            failed++;
                        }
                    }

                    Integer triggeredCount = registerWindowCount(counterKey, lockKey, failed, failureThreshold, windowSec);
                    if (triggeredCount != null) {
                        // System.out.println("Threshold reached for projectId: " + message.getProjectId() + ", count: " + triggeredCount);
                        sendFailureAlert(message.getProjectId(), triggeredCount, windowSec);
                    }
                }

                Integer latencyThresholdMs = rules.getAlertLatencyThresholdMs();
                Integer latencyBreachCount = rules.getAlertLatencyBreachCount();
                if (latencyThresholdMs != null && latencyBreachCount != null) {
                    int windowSec = rules.getAlertLatencyWindowSec() == null ? DEFAULT_WINDOW_SEC : rules.getAlertLatencyWindowSec();
                    String counterKey = "alerts:project:" + message.getProjectId() + ":latency_count";
                    String lockKey = "alerts:project:" + message.getProjectId() + ":latency_lock";

                    int breached = 0;
                    for (LifecycleLogRequest log : message.getLogs()) {
                        Long latencyMs = log.getLatencyMs();
                        if (latencyMs != null && latencyMs >= latencyThresholdMs) {
                            breached++;
                        }
                    }

                    Integer triggeredCount = registerWindowCount(counterKey, lockKey, breached, latencyBreachCount, windowSec);
                    if (triggeredCount != null) {
                        sendLatencyAlert(message.getProjectId(), latencyThresholdMs, triggeredCount, windowSec);
                    }
                }
                return;
            } catch (Exception e) {
                attempts++;
                if (attempts >= MAX_RETRIES) {
                    ingestionEventService.recordFailure(
                            message.getProjectId(),
                            "ALERT_FAILURE",
                            "log.alert.queue",
                            message.getLogs().size(),
                            attempts,
                            e.getMessage()
                    );
                }
            }
        }
    }

    private Integer registerWindowCount(String counterKey, String lockKey, int incrementBy, int threshold, int windowSec) {
        if (incrementBy <= 0) {
            return null;
        }
        Long count = redisTemplate.opsForValue().increment(counterKey, incrementBy);
        if (count != null && count == incrementBy) {
            redisTemplate.expire(counterKey, Duration.ofSeconds(windowSec));
        }

        if (count != null && count >= threshold) {
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(windowSec));
            if (locked != null && locked) {
                return count.intValue();
            }
        }
        return null;
    }

    private void sendFailureAlert(Long projectId, int count, int windowSec) {
        Project project = projectRepository.findWithUserById(projectId).orElse(null);
        if (project == null) {
            return;
        }
        String to = project.getAlertEmail();
        if (to == null || to.isBlank()) {
            return;
        }
        String subject = "LogFlow alert for project " + project.getName();
        String body = "Failure threshold reached. Failed count: " + count + " in " + windowSec + " seconds.";
        // System.out.println("Sending email to: " + to + ", subject: " + subject + ", body: " + body);
        emailService.send(to, subject, body);
        ProjectAlert alert = new ProjectAlert();
        alert.setProjectId(projectId);
        alert.setAlertType("FAILURE_THRESHOLD");
        alert.setMessage(body);
        alert.setTriggeredCount(count);
        alert.setTimeWindowSec(windowSec);
        alert.setSentTo(to);
        alert.setStatus("SENT");
        alertRepository.save(alert);
    }

    private void sendLatencyAlert(Long projectId, int thresholdMs, int breachCount, int windowSec) {
        Project project = projectRepository.findWithUserById(projectId).orElse(null);
        if (project == null) {
            return;
        }
        String to = project.getAlertEmail();
        if (to == null || to.isBlank()) {
            return;
        }
        String timestamp = Instant.now().toString();
        String subject = "LogFlow latency alert for project " + project.getName();
        String body = "Latency Alert: " + breachCount + " requests exceeded " + thresholdMs + "ms within " + windowSec + " seconds. Timestamp: " + timestamp + ".";
        emailService.send(to, subject, body);
        ProjectAlert alert = new ProjectAlert();
        alert.setProjectId(projectId);
        alert.setAlertType("LATENCY_THRESHOLD");
        alert.setMessage(body);
        alert.setTriggeredCount(breachCount);
        alert.setTimeWindowSec(windowSec);
        alert.setSentTo(to);
        alert.setStatus("SENT");
        alertRepository.save(alert);
    }
}
