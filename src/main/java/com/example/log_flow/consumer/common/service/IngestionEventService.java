package com.example.log_flow.consumer.common.service;

import com.example.log_flow.consumer.common.entity.IngestionEvent;
import com.example.log_flow.consumer.common.repository.IngestionEventRepository;
import org.springframework.stereotype.Service;

@Service
public class IngestionEventService {

    private final IngestionEventRepository repository;

    public IngestionEventService(IngestionEventRepository repository) {
        this.repository = repository;
    }

    public void recordFailure(Long projectId, String eventType, String queueName, int batchSize, int retryCount, String errorMessage) {
        IngestionEvent event = new IngestionEvent();
        event.setProjectId(projectId);
        event.setEventType(eventType);
        event.setQueueName(queueName);
        event.setBatchSize(batchSize);
        event.setRetryCount(retryCount);
        event.setStatus("FAILED");
        event.setErrorMessage(errorMessage);
        repository.save(event);
    }
}