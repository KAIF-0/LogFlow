package com.example.log_flow.project.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectServiceRequest {

        @Size(min = 2, max = 100, message = "Service name must be between 2 and 100 characters")
        @jakarta.validation.constraints.Pattern(
            regexp = "^[A-Za-z0-9 _-]+$",
            message = "Service name can contain letters, numbers, spaces, _ and -"
        )
    private String name;

        @Size(min = 1, max = 200, message = "Base path must be between 1 and 200 characters")
        @jakarta.validation.constraints.Pattern(
            regexp = "^/.*",
            message = "Base path must start with /"
        )
    private String basePath;

        @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    private Boolean isActive;
}
