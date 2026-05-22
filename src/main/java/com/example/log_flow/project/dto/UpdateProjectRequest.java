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

        @Size(min = 3, max = 100, message = "Project name must be between 3 and 100 characters")
        @jakarta.validation.constraints.Pattern(
            regexp = "^[A-Za-z0-9 _-]+$",
            message = "Project name can contain letters, numbers, spaces, _ and -"
        )
    private String name;

        @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;


    private ProjectEnvironment environment;
}