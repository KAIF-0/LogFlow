package com.example.log_flow.health;

import com.example.log_flow.common.response.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<ApiResponse<HealthStatus>> health() {
        HealthStatus status = new HealthStatus("UP", Instant.now());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), status));
    }

    @Data
    @AllArgsConstructor
    private static class HealthStatus {
        private String status;
        private Instant timestamp;
    }
}
