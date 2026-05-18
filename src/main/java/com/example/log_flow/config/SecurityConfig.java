package com.example.log_flow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.core.annotation.Order;

import com.example.log_flow.auth.security.CustomUserDetailsService;
import com.example.log_flow.auth.security.JwtFilter;
import com.example.log_flow.config.ratelimit.RateLimitFilter;
import com.example.log_flow.ingestion.security.IngestionAuthFilter;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtFilter jwtFilter;
    private final RateLimitFilter rateLimitFilter;
    private final IngestionAuthFilter ingestionAuthFilter;
    private final RequestLoggingFilter requestLoggingFilter;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AccessDeniedHandler accessDeniedHandler;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(
            CustomUserDetailsService userDetailsService,
            JwtFilter jwtFilter,
            RateLimitFilter rateLimitFilter,
            IngestionAuthFilter ingestionAuthFilter,
            RequestLoggingFilter requestLoggingFilter,
            AuthenticationEntryPoint authenticationEntryPoint,
            AccessDeniedHandler accessDeniedHandler,
            PasswordEncoder passwordEncoder
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.ingestionAuthFilter = ingestionAuthFilter;
        this.requestLoggingFilter = requestLoggingFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public AuthenticationManager authManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

        @Bean
        @Order(1)
        public SecurityFilterChain ingestionFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/logs/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session
                -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(ingestionAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
        }

        @Bean
        @Order(2)
        public SecurityFilterChain appFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session
                        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/health").permitAll()
                .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler)
                )
                .addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, RateLimitFilter.class);

        return http.build();
    }
}
