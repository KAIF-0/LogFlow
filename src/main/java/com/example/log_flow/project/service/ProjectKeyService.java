package com.example.log_flow.project.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class ProjectKeyService {

    private static final int RAW_KEY_BYTES = 32;
    private static final int PREFIX_LENGTH = 12;

    private final SecureRandom random = new SecureRandom();
    private final PasswordEncoder passwordEncoder;

    public ProjectKeyService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String generateRawKey(Long projectId) {
        byte[] bytes = new byte[RAW_KEY_BYTES];
        random.nextBytes(bytes);
        String suffix = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return "lf_" + projectId + "_" + suffix;
    }

    public String hashKey(String rawKey) {
        return passwordEncoder.encode(rawKey);
    }

    public String prefix(String rawKey) {
        if (rawKey == null || rawKey.length() < PREFIX_LENGTH) return rawKey;
        return rawKey.substring(0, PREFIX_LENGTH);
    }
}