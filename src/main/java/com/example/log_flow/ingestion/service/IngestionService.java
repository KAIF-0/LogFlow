package com.example.log_flow.ingestion.service;

import com.example.log_flow.ingestion.dto.BatchLogRequest;
import com.example.log_flow.ingestion.dto.ProjectContext;
import com.example.log_flow.ingestion.dto.ValidatedLogBatchMessage;
import com.example.log_flow.messaging.producer.LogProducer;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class IngestionService {

    private final LogProducer logProducer;

    public IngestionService(LogProducer logProducer) {
        this.logProducer = logProducer;
    }

    public void ingest(ProjectContext context, BatchLogRequest request) {
        ValidatedLogBatchMessage message = new ValidatedLogBatchMessage(
                context.getProjectId(),
                Instant.now(),
                request.getLogs()
        );
        logProducer.publishIngestionBatch(message);
    }
}