package com.example.log_flow.auth.service;

import com.example.log_flow.auth.dto.AuthResponse;
import com.example.log_flow.auth.dto.LoginRequest;
import com.example.log_flow.auth.dto.RegisterRequest;
import com.example.log_flow.auth.entity.User;
import com.example.log_flow.auth.repository.UserRepository;
import com.example.log_flow.auth.security.JwtService;
import com.example.log_flow.common.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new AppException(HttpStatus.CONFLICT, "email_taken", "Email already registered");
        }
        String hash = passwordEncoder.encode(req.getPassword());
        User user = new User(req.getName(), req.getEmail(), hash, "ROLE_USER");
        userRepository.save(user);
        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "invalid_credentials", "Invalid email or password"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "invalid_credentials", "Invalid email or password");
        }
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token);
    }
}
