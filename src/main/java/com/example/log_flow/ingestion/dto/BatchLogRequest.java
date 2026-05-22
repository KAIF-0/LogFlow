package com.example.log_flow.ingestion.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchLogRequest {

    @NotEmpty
    @jakarta.validation.constraints.Size(max = 1000, message = "Logs batch must be at most 1000 items")
    private List<@Valid LifecycleLogRequest> logs;
}