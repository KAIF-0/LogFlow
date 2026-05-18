package com.example.log_flow.project.controller;

import com.example.log_flow.common.response.ApiResponse;
import com.example.log_flow.project.dto.AlertEmailRequest;
import com.example.log_flow.project.dto.CreateProjectRequest;
import com.example.log_flow.project.dto.IngestionStatusRequest;
import com.example.log_flow.project.dto.ProjectResponse;
import com.example.log_flow.project.dto.UpdateProjectRequest;
import com.example.log_flow.project.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> create(@AuthenticationPrincipal UserDetails userDetails,
                                                               @Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.create(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> list(@AuthenticationPrincipal UserDetails userDetails) {
        List<ProjectResponse> response = projectService.list(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> get(@AuthenticationPrincipal UserDetails userDetails,
                                                            @PathVariable Long id) {
        ProjectResponse response = projectService.getById(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> update(@AuthenticationPrincipal UserDetails userDetails,
                                                               @PathVariable Long id,
                                                               @Valid @RequestBody UpdateProjectRequest request) {
        ProjectResponse response = projectService.update(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @PostMapping("/{id}/regenerate-key")
    public ResponseEntity<ApiResponse<ProjectResponse>> regenerate(@AuthenticationPrincipal UserDetails userDetails,
                                                                   @PathVariable Long id) {
        ProjectResponse response = projectService.regenerateKey(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @PatchMapping("/{id}/ingestion")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateIngestion(@AuthenticationPrincipal UserDetails userDetails,
                                                                        @PathVariable Long id,
                                                                        @Valid @RequestBody IngestionStatusRequest request) {
        ProjectResponse response = projectService.updateIngestion(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @PatchMapping("/{id}/alert-email")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateAlertEmail(@AuthenticationPrincipal UserDetails userDetails,
                                                                         @PathVariable Long id,
                                                                         @Valid @RequestBody AlertEmailRequest request) {
        ProjectResponse response = projectService.updateAlertEmail(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }
}