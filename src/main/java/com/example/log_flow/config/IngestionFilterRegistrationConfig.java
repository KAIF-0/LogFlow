package com.example.log_flow.config;

import com.example.log_flow.ingestion.security.IngestionAuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IngestionFilterRegistrationConfig {

    @Bean
    public FilterRegistrationBean<IngestionAuthFilter> ingestionAuthFilterRegistration(IngestionAuthFilter filter) {
        FilterRegistrationBean<IngestionAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}