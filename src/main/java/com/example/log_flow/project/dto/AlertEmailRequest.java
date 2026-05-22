package com.example.log_flow.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertEmailRequest {

    @NotBlank
    @Email
    @jakarta.validation.constraints.Size(max = 255, message = "Alert email must be at most 255 characters")
    private String alertEmail;
}
