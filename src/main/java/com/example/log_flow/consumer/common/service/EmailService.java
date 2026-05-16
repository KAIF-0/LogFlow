package com.example.log_flow.consumer.common.service;

public interface EmailService {
    void send(String to, String subject, String body);
}