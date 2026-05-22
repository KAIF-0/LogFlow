package com.example.log_flow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Email
    @NotBlank
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @NotBlank
    @Size(min = 6, max = 72, message = "Password must be between 6 and 72 characters")
    private String password;
}
