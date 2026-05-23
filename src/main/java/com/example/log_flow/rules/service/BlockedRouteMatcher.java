package com.example.log_flow.rules.service;

import java.util.ArrayList;
import java.util.List;

public final class BlockedRouteMatcher {

    private BlockedRouteMatcher() {}

    public static boolean isBlocked(List<String> patterns, String path) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        String normalizedPath = normalizePath(path);
        for (String pattern : patterns) {
            String normalizedPattern = normalizePattern(pattern);
            if (matchesPattern(normalizedPattern, normalizedPath)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> normalizePatterns(List<String> patterns) {
        if (patterns == null) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String pattern : patterns) {
            normalized.add(normalizePattern(pattern));
        }
        return normalized;
    }

    public static List<String> findInvalidPatterns(List<String> patterns) {
        if (patterns == null) {
            return List.of();
        }
        List<String> invalid = new ArrayList<>();
        for (String pattern : patterns) {
            if (!isValidPattern(pattern)) {
                invalid.add(pattern);
            }
        }
        return invalid;
    }

    private static boolean matchesPattern(String pattern, String path) {
        if ("/".equals(pattern)) {
            return "/".equals(path);
        }
        if (pattern.endsWith("/*")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            if (prefix.isEmpty()) {
                return path.length() > 1;
            }
            return path.startsWith(prefix + "/") && path.length() > prefix.length();
        }

        String[] patternParts = pattern.split("/");
        String[] pathParts = path.split("/");
        if (patternParts.length != pathParts.length) {
            return false;
        }
        for (int i = 1; i < patternParts.length; i++) {
            String patternPart = patternParts[i];
            String pathPart = pathParts[i];
            if ("*".equals(patternPart)) {
                if (pathPart.isBlank()) {
                    return false;
                }
                continue;
            }
            if (!patternPart.equals(pathPart)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidPattern(String pattern) {
        try {
            normalizePattern(pattern);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static String normalizePattern(String pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern cannot be null");
        }
        String trimmed = pattern.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be empty");
        }
        if (!trimmed.startsWith("/")) {
            throw new IllegalArgumentException("Pattern must start with /");
        }
        if (trimmed.length() > 1 && trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        String[] parts = trimmed.split("/");
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) {
                throw new IllegalArgumentException("Empty path segment is not allowed");
            }
            if (part.contains("*") && !"*".equals(part)) {
                throw new IllegalArgumentException("Only single '*' segment is supported");
            }
        }
        return trimmed;
    }

    private static String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        String trimmed = path.trim();
        if (!trimmed.startsWith("/")) {
            trimmed = "/" + trimmed;
        }
        if (trimmed.length() > 1 && trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
