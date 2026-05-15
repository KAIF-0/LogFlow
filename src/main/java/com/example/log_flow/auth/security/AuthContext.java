package com.example.log_flow.auth.security;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.log_flow.auth.entity.User;
import com.example.log_flow.auth.repository.UserRepository;
import com.example.log_flow.common.exception.AppException;

@Service
public class AuthContext {

    private final UserRepository userRepository;

    public AuthContext(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User requireUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "unauthorized", "Unauthorized");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "user_not_found", "User not found"));
    }
}
