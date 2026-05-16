package com.example.log_flow.rules.controller;

import com.example.log_flow.common.response.ApiResponse;
import com.example.log_flow.rules.dto.RulesRequest;
import com.example.log_flow.rules.dto.RulesResponse;
import com.example.log_flow.rules.service.RulesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects/{projectId}/rules")
public class RulesController {

    private final RulesService rulesService;

    public RulesController(RulesService rulesService) {
        this.rulesService = rulesService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<RulesResponse>> getRules(@AuthenticationPrincipal UserDetails userDetails,
                                                               @PathVariable Long projectId) {
        RulesResponse response = rulesService.getRules(userDetails.getUsername(), projectId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<RulesResponse>> upsertRules(@AuthenticationPrincipal UserDetails userDetails,
                                                                  @PathVariable Long projectId,
                                                                  @Valid @RequestBody RulesRequest request) {
        RulesResponse response = rulesService.upsertRules(userDetails.getUsername(), projectId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }
}