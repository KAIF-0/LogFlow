package com.example.log_flow.consumer.alert.service;

import java.time.Duration;

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
                if (rules == null || rules.getAlertFailureThreshold() == null) {
                    return;
                }
                int threshold = rules.getAlertFailureThreshold();
                int windowSec = rules.getAlertTimeWindowSec() == null ? DEFAULT_WINDOW_SEC : rules.getAlertTimeWindowSec();
                String counterKey = "alerts:project:" + message.getProjectId() + ":error_window";
                String lockKey = "alerts:project:" + message.getProjectId() + ":lock";

                int failed = 0;
                for (LifecycleLogRequest log : message.getLogs()) {
                    if (log.getStatusCode() >= 500) {
                        failed++;
                    }
                }

                if (failed == 0) {
                    return;
                }

                Long count = redisTemplate.opsForValue().increment(counterKey, failed);
                if (count != null && count == failed) {
                    redisTemplate.expire(counterKey, Duration.ofSeconds(windowSec));
                }

                if (count != null && count >= threshold) {
                    Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(windowSec));
                    if (locked != null && locked) {
                        // System.out.println("Threshold reached for projectId: " + message.getProjectId() + ", count: " + count);
                        sendAlert(message.getProjectId(), count.intValue(), windowSec);
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

    private void sendAlert(Long projectId, int count, int windowSec) {
        Project project = projectRepository.findWithUserById(projectId).orElse(null);
        if (project == null || project.getUser() == null) {
            return;
        }
        String to = project.getUser().getEmail();
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
}
