package com.example.log_flow.auth.controller;

import com.example.log_flow.auth.dto.AuthResponse;
import com.example.log_flow.auth.dto.LoginRequest;
import com.example.log_flow.auth.dto.RegisterRequest;
import com.example.log_flow.auth.dto.UserResponse;
import com.example.log_flow.auth.mapper.UserMapper;
import com.example.log_flow.auth.service.AuthService;
import com.example.log_flow.auth.repository.UserRepository;
import com.example.log_flow.common.exception.AppException;
import com.example.log_flow.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse resp = authService.register(req);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), resp));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse resp = authService.login(req);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), resp));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal UserDetails details) {
        if (details == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "unauthorized", "Unauthorized");
        }
        UserResponse user = authService.getCurrentUser(details.getUsername());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), user));
    }
}
