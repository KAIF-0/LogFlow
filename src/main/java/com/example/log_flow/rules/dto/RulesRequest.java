package com.example.log_flow.rules.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RulesRequest {

    @NotNull
    @Min(1)
    @Max(10240)
    private Integer maxPayloadSizeKb;

    @NotNull
    private List<String> blockedRoutes;

    @NotNull
    private List<String> maskedFields;

    @NotNull
    private List<String> headerWhitelist;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer samplingRate;

    @NotNull
    @Min(1)
    private Integer rateLimitPerMin;

    @Min(1)
    private Integer alertFailureThreshold;

    @Min(1)
    private Integer alertTimeWindowSec;
}