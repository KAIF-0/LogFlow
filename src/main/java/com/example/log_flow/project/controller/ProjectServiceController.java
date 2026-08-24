package com.example.log_flow.project.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.example.log_flow.common.response.ApiResponse;
import com.example.log_flow.project.dto.ProjectServiceRequest;
import com.example.log_flow.project.dto.ProjectServiceResponse;
import com.example.log_flow.project.dto.UpdateProjectServiceRequest;
import com.example.log_flow.project.service.ProjectServiceManager;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/projects/{projectId}/services")
@SecurityRequirement(name = "bearerAuth")
public class ProjectServiceController {

    private final ProjectServiceManager projectServiceManager;

    public ProjectServiceController(ProjectServiceManager projectServiceManager) {
        this.projectServiceManager = projectServiceManager;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectServiceResponse>> create(@AuthenticationPrincipal UserDetails userDetails,
                                                                      @PathVariable Long projectId,
                                                                      @Valid @RequestBody ProjectServiceRequest request) {
        ProjectServiceResponse response = projectServiceManager.create(userDetails.getUsername(), projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED.value(), response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectServiceResponse>>> list(@AuthenticationPrincipal UserDetails userDetails,
                                                                          @PathVariable Long projectId) {
        List<ProjectServiceResponse> response = projectServiceManager.list(userDetails.getUsername(), projectId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<ProjectServiceResponse>> get(@AuthenticationPrincipal UserDetails userDetails,
                                                                    @PathVariable Long projectId,
                                                                    @PathVariable Long serviceId) {
        ProjectServiceResponse response = projectServiceManager.get(userDetails.getUsername(), projectId, serviceId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @PutMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<ProjectServiceResponse>> update(@AuthenticationPrincipal UserDetails userDetails,
                                                                       @PathVariable Long projectId,
                                                                       @PathVariable Long serviceId,
                                                                       @Valid @RequestBody UpdateProjectServiceRequest request) {
        ProjectServiceResponse response = projectServiceManager.update(userDetails.getUsername(), projectId, serviceId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal UserDetails userDetails,
                                                     @PathVariable Long projectId,
                                                     @PathVariable Long serviceId) {
        projectServiceManager.delete(userDetails.getUsername(), projectId, serviceId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), null));
    }
}
