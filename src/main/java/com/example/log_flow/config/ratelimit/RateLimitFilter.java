package com.example.log_flow.config.ratelimit;

import com.example.log_flow.common.response.ApiResponse;
import com.example.log_flow.common.response.ErrorInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimitService rateLimitService, ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //per IP + method + URI rate limiting
        String clientKey = request.getRemoteAddr() + ":" + request.getMethod() + ":" + request.getRequestURI();
        if (!rateLimitService.tryConsume(clientKey)) {
            ErrorInfo errorInfo = new ErrorInfo("rate_limit_exceeded", "Too many requests!", null);
            ApiResponse<Void> apiResponse = ApiResponse.failure(HttpStatus.TOO_MANY_REQUESTS.value(), errorInfo);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            objectMapper.writeValue(response.getWriter(), apiResponse);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
