package com.example.log_flow.project.dto;

import com.example.log_flow.common.enums.ProjectEnvironment;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectRequest {

    @Size(min = 3, max = 100)
    private String name;

    @Size(max = 500)
    private String description;


    private ProjectEnvironment environment;
}