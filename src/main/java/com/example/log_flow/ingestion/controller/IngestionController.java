package com.example.log_flow.ingestion.controller;

import com.example.log_flow.common.response.ApiResponse;
import com.example.log_flow.ingestion.dto.BatchLogRequest;
import com.example.log_flow.ingestion.dto.ProjectContext;
import com.example.log_flow.ingestion.service.IngestionService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logs")
@SecurityRequirement(name = "apiKeyAuth")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<ApiResponse<Void>> ingest(@RequestAttribute("projectContext") ProjectContext context,
                                                    @Valid @RequestBody BatchLogRequest request) {
        ingestionService.ingest(context, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(HttpStatus.ACCEPTED.value(), null));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validate(@RequestAttribute("projectContext") ProjectContext context) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "ok"));
    }
}